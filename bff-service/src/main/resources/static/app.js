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
const show = (el, data) => { el.textContent = typeof data === "string" ? data : JSON.stringify(data, null, 2); };

async function init() {
  const resp = await msalInstance.handleRedirectPromise();
  if (resp && resp.account) setSession(resp);
  else {
    const accts = msalInstance.getAllAccounts();
    if (accts.length) { account = accts[0]; await refreshToken(); renderUser(); }
  }
}

function setSession(resp) {
  account = resp.account;
  idToken = resp.idToken;
  renderUser();
}

function renderUser() {
  if (account) {
    const claims = account.idTokenClaims || {};
    const rol = claims.extension_rol || "(sin rol)";
    $("userInfo").textContent = `${account.name || account.username} · rol: ${rol}`;
    $("btnLogin").classList.add("hidden");
    $("btnLogout").classList.remove("hidden");
  }
}

async function login() {
  try {
    const resp = await msalInstance.loginPopup({ scopes: APP_CONFIG.b2c.scopes });
    setSession(resp);
  } catch (e) { alert("Error de login: " + e.message); }
}

function logout() { msalInstance.logoutPopup(); }

async function refreshToken() {
  try {
    const resp = await msalInstance.acquireTokenSilent({ scopes: APP_CONFIG.b2c.scopes, account });
    idToken = resp.idToken;
  } catch (e) { console.warn("acquireTokenSilent fallo, se requiere login", e); }
}

async function api(method, path, body) {
  if (!idToken) await refreshToken();
  const opts = {
    method,
    headers: { "Authorization": "Bearer " + idToken }
  };
  if (body) { opts.headers["Content-Type"] = "application/json"; opts.body = JSON.stringify(body); }
  const res = await fetch(APP_CONFIG.apiBase + path, opts);
  const text = await res.text();
  let data; try { data = JSON.parse(text); } catch { data = text; }
  return { status: res.status, data };
}

// ── Handlers de la UI ───────────────────────────────────────
$("btnLogin").onclick = login;
$("btnLogout").onclick = logout;

$("btnCursos").onclick = async () => show($("outCursos"), await api("GET", "/cursos"));

$("btnCrearCurso").onclick = async () => {
  const body = {
    codigo: $("cCodigo").value, nombre: $("cNombre").value,
    instructor: $("cInstructor").value, cupos: Number($("cCupos").value)
  };
  show($("outCrearCurso"), await api("POST", "/cursos", body));
};

$("btnPublicar").onclick = async () =>
  show($("outPublicar"), await api("POST", "/inscripciones/publicar", { cursoCodigo: $("iCurso").value }));

$("btnConsumir").onclick = async () =>
  show($("outConsumir"), await api("POST", "/inscripciones/consumir"));

$("btnMatriculas").onclick = async () => show($("outMatriculas"), await api("GET", "/matriculas"));

init();
