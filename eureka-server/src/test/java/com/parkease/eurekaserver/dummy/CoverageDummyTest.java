package com.parkease.eurekaserver.dummy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CoverageDummyTest {
    @Autowired
    private CoverageDummy dummy;
    @Test
    void test() {
        dummy.test();
    }
}
