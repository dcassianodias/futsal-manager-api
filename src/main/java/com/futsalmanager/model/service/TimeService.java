package com.futsalmanager.model.service;

import com.futsalmanager.model.repositories.TimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TimeService {

    @Autowired
    private TimeRepository repository;


}
