package com.pascloud.module.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasCloudConfig {
	
	@Value("${project.config}")
	private String PASCLOUD_SPRINGXML_DIR;
	
	@Value("${project.xml}")
	private String PASCLOUD_SPRINGXML_PATH;

	public String getPASCLOUD_SPRINGXML_PATH() {
		return PASCLOUD_SPRINGXML_PATH;
	}

	public void setPASCLOUD_SPRINGXML_PATH(String pASCLOUD_SPRINGXML_PATH) {
		PASCLOUD_SPRINGXML_PATH = pASCLOUD_SPRINGXML_PATH;
	}

	public String getPASCLOUD_SPRINGXML_DIR() {
		return PASCLOUD_SPRINGXML_DIR;
	}

	public void setPASCLOUD_SPRINGXML_DIR(String pASCLOUD_SPRINGXML_DIR) {
		PASCLOUD_SPRINGXML_DIR = pASCLOUD_SPRINGXML_DIR;
	}
	
	

	
}
