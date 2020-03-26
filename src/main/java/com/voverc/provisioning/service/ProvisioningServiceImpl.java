package com.voverc.provisioning.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voverc.provisioning.entity.Device;
import com.voverc.provisioning.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Service
public class ProvisioningServiceImpl implements ProvisioningService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Value("${provisioning.domain}")
    private String domain;

    @Value("${provisioning.port}")
    private String port;

    @Value("${provisioning.codecs}")
    private List<String> codecs;

    public String getProvisioningFile(String macAddress) throws IOException {
        Device device = deviceRepository.getByMacAddress(macAddress);
        if (device == null) {
            throw new NoSuchElementException(String.format("Device cannot be found with mac address: %s", macAddress));
        }

        String provisioningFile = null;
        Map<String, Object> provisioningMap = new HashMap<>();
        provisioningMap.put("username", device.getUsername());
        provisioningMap.put("password", device.getPassword());
        provisioningMap.put("domain", domain);
        provisioningMap.put("port", port);
        provisioningMap.put("codecs", codecs);
        if (Device.DeviceModel.CONFERENCE.equals(device.getModel())) {
            ObjectMapper objectMapper = new ObjectMapper();
            if (!StringUtils.isEmpty(device.getOverrideFragment())) {
                Map<String, String> overrideFragmentMap = objectMapper.readValue(device.getOverrideFragment(), new TypeReference<Map<String, String>>() {
                });
                provisioningMap.putAll(overrideFragmentMap);
            }
            provisioningFile = objectMapper.writeValueAsString(provisioningMap);
        } else if (Device.DeviceModel.DESK.equals(device.getModel())) {
            Properties properties = new Properties();
            properties.putAll(provisioningMap);
            if (!StringUtils.isEmpty(device.getOverrideFragment())) {
                properties.load(new StringReader(device.getOverrideFragment()));
            }
            provisioningFile = getPropertiesAsFile(properties);
        }
        return provisioningFile;
    }

    private String getPropertiesAsFile(Properties properties) {
        StringBuilder propertiesAsFileBuilder = new StringBuilder();
        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
            propertiesAsFileBuilder.append(objectObjectEntry.getKey()).append("=").append(objectObjectEntry.getValue()).append("\n");
        }
        return propertiesAsFileBuilder.toString().replaceAll("[\\[\\]]", "");
    }
}
