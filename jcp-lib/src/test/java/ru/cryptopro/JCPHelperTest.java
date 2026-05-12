package ru.cryptopro;

import org.junit.jupiter.api.Test;

class JCPHelperTest {

    @Test
    void testInitJCP() {
        JCPHelper.initJCP();
    }

    @Test
    void testInitJCPTLS() {
        JCPHelper.initJCPWithTLS();
    }
}