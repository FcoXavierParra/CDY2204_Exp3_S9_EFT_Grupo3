// ============================================================
//  Configuración del frontend (Azure AD B2C + API del BFF)
//  Reemplaza los valores segun tu tenant. Estos vienen de S8.
// ============================================================
const APP_CONFIG = {
  // API del BFF: relativo (el BFF sirve este frontend) o la URL del API Gateway.
  apiBase: "/api/bff",

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
