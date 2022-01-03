package com.enuri.plsync.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enuri.plsync.service.JobService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JobController {

	private final JobService jobService;

	@GetMapping("/job/mainSyncRun")
	public ResponseEntity<String> mainSyncRun() {
		log.info("mainSyncRun~!!");
		return new ResponseEntity<>(jobService.mainSyncJob(), HttpStatus.OK);
	}

	@GetMapping("/job/elocSyncRun")
	public ResponseEntity<String> elocSyncRun() {
		log.info("elocSyncRun~!!");
		return new ResponseEntity<>(jobService.elocSyncJob(), HttpStatus.OK);
	}

	@GetMapping("/job/maintest")
	public ResponseEntity<String> maintest() {
		log.info("/job/maintest~~~~!!");
		return new ResponseEntity<>("main test!!!!", HttpStatus.OK);
	}

	@GetMapping("/job/eloctest")
	public ResponseEntity<String> eloctest() {
		log.info("/job/eloctest~~~~!!");
		return new ResponseEntity<>("eloc test!!!!", HttpStatus.OK);
	}
}