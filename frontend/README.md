# Frontend

El frontend (HTML + JS + MSAL.js) se sirve desde el **bff-service** para simplificar el despliegue.

Archivos reales en: [`../bff-service/src/main/resources/static/`](../bff-service/src/main/resources/static/)
- `index.html` — UI (login B2C, cursos, inscripción→cola, consumir, matrículas)
- `config.js` — configuración de Azure AD B2C y URL del BFF
- `app.js` — lógica MSAL + llamadas al BFF
- `styles.css`

Al levantar el BFF, el frontend queda en `http://<host>:8080/`.

> Recuerda registrar el origen del frontend (ej. `http://107.23.67.117`) como **Redirect URI de tipo SPA** en la app de Azure AD B2C.
