/* ============================================================
   Lógica del frontend: login Azure AD B2C (MSAL) + llamadas al BFF
   ============================================================ */
const msalInstance = new msal.PublicClientApplication({
  auth: {
    clientId: APP_CONFIG.b2c.clientId,
    authority: APP_CONFIG.b2c.authority,
    knownAuthorities: APP_CONFIG.b2c.knownAuthorities,
    redirectUri: APP_CONFIG.b2c.redirectUri
  },
  cache: { cacheLocation: "sessionStorage" }
});

let account = null;
let idToken = null;

const $ = (id) => document.getElementById(id);

// ── Helpers de presentación ─────────────────────────────────
function esc(s) { return String(s == null ? "" : s).replace(/[&<>]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;" }[c])); }

/** Muestra un mensaje elegante (ok/err/info) + JSON técnico plegable. */
function render(el, { ok = true, title = "", html = "", raw = null } = {}) {
  const cls = ok === null ? "msg-info" : (ok ? "msg-ok" : "msg-err");
  const icon = ok === null ? "ℹ️" : (ok ? "✅" : "⛔");
  let out = `<div class="msg ${cls}">${icon} ${esc(title)}</div>`;
  if (html) out += html;
  if (raw !== null) {
    out += `<details class="jsonbox"><summary>ver JSON</summary><pre>${esc(JSON.stringify(raw, null, 2))}</pre></details>`;
  }
  el.innerHTML = out;
}

function tablaCursos(cursos) {
  if (!Array.isArray(cursos) || cursos.length === 0) return `<p class="muted">No hay cursos.</p>`;
  const filas = cursos.map(c =>
    `<tr><td>${esc(c.codigo)}</td><td>${esc(c.nombre)}</td><td>${esc(c.instructor)}</td><td>${esc(c.cupos)}</td></tr>`).join("");
  return `<table class="tabla"><thead><tr><th>Código</th><th>Nombre</th><th>Instructor</th><th>Cupos</th></tr></thead><tbody>${filas}</tbody></table>`;
}

function tablaMatriculas(ms) {
  if (!Array.isArray(ms) || ms.length === 0) return `<p class="muted">Sin matrículas aún.</p>`;
  const filas = ms.map(m =>
    `<tr><td>${esc(m.cursoCodigo)}</td><td>${esc(m.estudianteEmail)}</td><td class="fecha">${esc((m.fechaMatricula||"").toString().replace("T"," ").slice(0,19))}</td><td><span class="badge">${esc(m.estado)}</span></td></tr>`).join("");
  return `<table class="tabla"><thead><tr><th>Curso</th><th>Estudiante</th><th>Fecha</th><th>Estado</th></tr></thead><tbody>${filas}</tbody></table>`;
}

// ── Autenticación (MSAL) ────────────────────────────────────
async function init() {
  const resp = await msalInstance.handleRedirectPromise();
  if (resp && resp.account) setSession(resp);
  else {
    const accts = msalInstance.getAllAccounts();
    if (accts.length) { account = accts[0]; await refreshToken(); renderUser(); }
  }
}
function setSession(resp) { account = resp.account; idToken = resp.idToken; renderUser(); }
function renderUser() {
  if (account) {
    const claims = account.idTokenClaims || {};
    $("userInfo").textContent = `${account.name || account.username} · rol: ${claims.extension_rol || "(sin rol)"}`;
    $("btnLogin").classList.add("hidden");
    $("btnLogout").classList.remove("hidden");
  }
  applyRole();
}

/** Perfil por rol: muestra las tarjetas de administración solo al INSTRUCTOR. */
function applyRole() {
  const claims = (account && account.idTokenClaims) || {};
  const esInstructor = String(claims.extension_rol || "").toLowerCase() === "instructor";
  document.querySelectorAll(".solo-instructor").forEach(el => el.classList.toggle("hidden", !esInstructor));
  const hint = $("hintEstudiante");
  if (hint) hint.classList.toggle("hidden", !account || esInstructor);
}
async function login() {
  try { setSession(await msalInstance.loginPopup({ scopes: APP_CONFIG.b2c.scopes })); }
  catch (e) { alert("Error de login: " + e.message); }
}
function logout() { msalInstance.logoutPopup().then(() => location.reload()).catch(() => location.reload()); }
async function refreshToken() {
  try { idToken = (await msalInstance.acquireTokenSilent({ scopes: APP_CONFIG.b2c.scopes, account })).idToken; }
  catch (e) { console.warn("acquireTokenSilent falló", e); }
}

async function api(method, path, body) {
  if (!idToken) await refreshToken();
  const opts = { method, headers: { "Authorization": "Bearer " + idToken } };
  if (body) { opts.headers["Content-Type"] = "application/json"; opts.body = JSON.stringify(body); }
  const res = await fetch(APP_CONFIG.apiBase + path, opts);
  const text = await res.text();
  let data; try { data = JSON.parse(text); } catch { data = text; }
  return { status: res.status, data };
}

// ── Handlers de la UI ───────────────────────────────────────
$("btnLogin").onclick = login;
$("btnLogout").onclick = logout;

$("btnCursos").onclick = async () => {
  const r = await api("GET", "/cursos");
  if (r.status === 200) render($("outCursos"), { ok: true, title: `${(r.data||[]).length} curso(s) disponibles`, html: tablaCursos(r.data), raw: r.data });
  else render($("outCursos"), { ok: false, title: `Error (${r.status})`, raw: r.data });
};

$("btnCrearCurso").onclick = async () => {
  const body = { codigo: $("cCodigo").value, nombre: $("cNombre").value, instructor: $("cInstructor").value, cupos: Number($("cCupos").value) };
  const r = await api("POST", "/cursos", body);
  if (r.status === 201) render($("outCrearCurso"), { ok: true, title: `Curso "${r.data.codigo}" creado (id ${r.data.id})`, raw: r.data });
  else if (r.status === 403) render($("outCrearCurso"), { ok: false, title: "403 · No tienes permiso (se requiere rol INSTRUCTOR)", raw: r.data });
  else render($("outCrearCurso"), { ok: false, title: `Error (${r.status})`, raw: r.data });
};

$("btnMaterial").onclick = async () => {
  const codigo = $("mCurso").value.trim();
  const file = $("mArchivo").files[0];
  if (!codigo || !file) {
    render($("outMaterial"), { ok: false, title: "Indica el código del curso y selecciona un archivo" });
    return;
  }
  if (!idToken) await refreshToken();
  const fd = new FormData();
  fd.append("archivo", file);
  const res = await fetch(APP_CONFIG.apiBase + "/cursos/" + encodeURIComponent(codigo) + "/material", {
    method: "POST", headers: { "Authorization": "Bearer " + idToken }, body: fd
  });
  const text = await res.text();
  let data; try { data = JSON.parse(text); } catch { data = text; }
  if (res.status === 200) render($("outMaterial"), { ok: true, title: `Material subido a AWS S3: ${esc(file.name)}`, html: `<p class="muted">Guardado en el bucket S3 → <code>${esc((data && data.materialS3Key) || ("materiales/" + codigo + "/" + file.name))}</code></p>`, raw: data });
  else if (res.status === 403) render($("outMaterial"), { ok: false, title: "403 · se requiere rol INSTRUCTOR", raw: data });
  else render($("outMaterial"), { ok: false, title: `Error (${res.status})`, raw: data });
};

$("btnPublicar").onclick = async () => {
  const r = await api("POST", "/inscripciones/publicar", { cursoCodigo: $("iCurso").value });
  if (r.status === 202) render($("outPublicar"), { ok: true, title: `Inscripción a "${r.data.cursoCodigo}" enviada a la COLA 1`, html: `<p class="muted">Productor → RabbitMQ. Ahora usa "Consumir".</p>`, raw: r.data });
  else render($("outPublicar"), { ok: false, title: `Error (${r.status})`, raw: r.data });
};

$("btnConsumir").onclick = async () => {
  const r = await api("POST", "/inscripciones/consumir");
  const m = r.data && r.data.matricula;
  if (r.status === 201 && m) render($("outConsumir"), { ok: true, title: `Matrícula guardada en Oracle: ${m.estudianteEmail} → ${m.cursoCodigo}`, html: `<p class="muted">Consumidor leyó la cola y persistió (estado ${esc(m.estado)}).</p>`, raw: r.data });
  else render($("outConsumir"), { ok: null, title: r.data && r.data.mensaje ? r.data.mensaje : "La cola está vacía", raw: r.data });
};

$("btnMatriculas").onclick = async () => {
  const r = await api("GET", "/matriculas");
  if (r.status === 200) render($("outMatriculas"), { ok: true, title: `${(r.data||[]).length} matrícula(s) en Oracle`, html: tablaMatriculas(r.data), raw: r.data });
  else render($("outMatriculas"), { ok: false, title: `Error (${r.status})`, raw: r.data });
};

init();
