package com.aircargo.loadplanningservice.service;

import com.aircargo.loadplanningservice.dto.LoadPlanningImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface LoadPlanningImportService {
    LoadPlanningImportResultDTO importLoadPlanning(MultipartFile file) throws IOException;
}
