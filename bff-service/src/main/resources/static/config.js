// ============================================================
//  Configuración del frontend (Azure AD B2C + API del BFF)
//  Reemplaza los valores segun tu tenant. Estos vienen de S8.
// ============================================================
// ── MODO DE EJECUCIÓN ───────────────────────────────────────
// El login de Azure B2C se hace siempre en el navegador (localhost).
// El backend puede consumirse LOCAL o en la NUBE (EC2) vía API Gateway.
const API_LOCAL = "/api/bff";                                                       // todo en tu PC
const API_NUBE  = "https://e9w0i9cwwf.execute-api.us-east-1.amazonaws.com/api/bff"; // backend en EC2 (API Gateway)
// ▼ Cambia SOLO esta línea para alternar el modo:
const API_BASE  = API_NUBE;   // usa API_NUBE para el modo "frontend local + backend EC2"

const APP_CONFIG = {
  // API del BFF: local (el BFF sirve este frontend) o la URL del API Gateway (nube).
  apiBase: API_BASE,

  // Azure AD B2C
  b2c: {
    clientId: "a1ac2094-5d55-4792-a708-cccf87b6f6de",
    authority: "https://gestionpedidosfp.b2clogin.com/gestionpedidosfp.onmicrosoft.com/B2C_1_signupsignin",
    knownAuthorities: ["gestionpedidosfp.b2clogin.com"],
    // redirectUri = origen actual (debe estar registrado como SPA en Azure)
    redirectUri: window.location.origin,
    // Se usa el id_token (aud = clientId) como Bearer, igual que valida el backend.
    scopes: ["openid"]
  }
};
