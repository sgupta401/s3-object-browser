package com.intuit.service;

import java.util.List;


public class JwkResponse {
    private List<JwkKey> keys;


    public static class JwkKey {
        private String kty; // Key type
        private String e;   // Exponent
        private String kid; // Key ID
        private String n;

        public String getKty() {
            return kty;
        }

        public void setKty(String kty) {
            this.kty = kty;
        }

        public String getE() {
            return e;
        }

        public void setE(String e) {
            this.e = e;
        }

        public String getKid() {
            return kid;
        }

        public void setKid(String kid) {
            this.kid = kid;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            this.n = n;
        }

        // Modulus
    }

    public List<JwkKey> getKeys() {
        return keys;
    }

    public void setKeys(List<JwkKey> keys) {
        this.keys = keys;
    }
}