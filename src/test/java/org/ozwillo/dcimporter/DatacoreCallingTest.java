package org.ozwillo.dcimporter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.ozwillo.dcimporter.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatacoreCallingTest {

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private DatacoreClient datacoreClient;

    public DatacoreCallingTest() {
    }

    @Test
    public void testCallingDatacore() {
        systemUserService.runAs(() -> {
            List<DCResource> dcRessources = datacoreClient.findResources("oasis.main", "geo:Area_0");
            Assert.assertTrue(!dcRessources.isEmpty());
        });
    }
}
