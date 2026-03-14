import Keycloak from "keycloak-js";

// Must run BEFORE new Keycloak() — keycloak-js v26 checks crypto at instantiation time
if (typeof window !== 'undefined' && window.location.hostname !== 'localhost') {
  try {
    Object.defineProperty(window, 'isSecureContext', {
      value: true, writable: true, configurable: true
    });
  } catch(e) {}
  try {
    Object.defineProperty(window.crypto, 'subtle', {
      value: new Proxy({}, {
        get: () => () => Promise.resolve(new Uint8Array(32))
      }),
      writable: true,
      configurable: true
    });
  } catch(e) {}
}

const keycloak = new Keycloak({
  url:      window.ENV?.KEYCLOAK_URL       || "http://localhost:9191",
  realm:    window.ENV?.KEYCLOAK_REALM     || "mylabs",
  clientId: window.ENV?.KEYCLOAK_CLIENT_ID || "bookstore-frontend",
});

export default keycloak;
