import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url:      window.ENV?.KEYCLOAK_URL       || "http://localhost:9191",
  realm:    window.ENV?.KEYCLOAK_REALM     || "mylabs",
  clientId: window.ENV?.KEYCLOAK_CLIENT_ID || "bookstore-frontend",
});

export default keycloak;
