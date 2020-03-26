package com.voverc.provisioning.controller;

import com.voverc.provisioning.service.ProvisioningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class ProvisioningController {
    @Autowired
    private ProvisioningService provisioningService;


    @RequestMapping(value = "provisioning/{macAddress}", method = RequestMethod.GET)
    public ResponseEntity<String> getProvisioningFile(@PathVariable String macAddress) throws IOException {
        String provisioningFile = provisioningService.getProvisioningFile(macAddress);
        return ResponseEntity.ok().body(provisioningFile);
    }
}