package ru.cryptopro;

import org.junit.jupiter.api.Test;

class JCSPHelperTest {

    @Test
    void testInitJCSP() {
        JCSPHelper.initJCSP();
    }

    @Test
    void testInitJCSPTLS() {
        JCSPHelper.initJCSPWithTLS();
    }
}