package com.url.shortening.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import com.url.shortening.common.URLValidator;
import com.url.shortening.service.URLConverterService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

@RestController
public class MyController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MyController.class);
	private final URLConverterService urlConverterService;

	public MyController(URLConverterService urlConverterService) {
		this.urlConverterService = urlConverterService;
	}

	@RequestMapping(value = "/myurlshortener", method = RequestMethod.POST, consumes = { "application/json" })
	@ResponseBody
	public ResponseEntity<String> shortenUrl(@RequestBody final ShortenRequest shortenRequest,
			HttpServletRequest request) throws Exception {
		LOGGER.info("Input url to shorten: " + shortenRequest.getUrl());
		String longUrl = shortenRequest.getUrl();
		if (URLValidator.INSTANCE.validateURL(longUrl)) {
			String localURL = request.getRequestURL().toString();
			String shortenedUrl = urlConverterService.shortenURL(localURL, shortenRequest.getUrl());
			LOGGER.info("Shortened url to: " + shortenedUrl);
			shortenedUrl = "{\n \"shortenedurl\": \""+ shortenedUrl +"\" \n}";
			return new ResponseEntity<>(shortenedUrl, HttpStatus.OK);
		} else {

			String ss = "{ \"error\" : \"400\",\"error_description\" : \"Invalid input URL.\" }";
			return new ResponseEntity<>(ss, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public RedirectView redirectUrl(@PathVariable String id, HttpServletRequest request, HttpServletResponse response)
			throws IOException, URISyntaxException, Exception {
		LOGGER.info("Input shortened url to redirect: " + id);
		String redirectUrlString = urlConverterService.getLongURLFromID(id);
		LOGGER.info("Original URL: " + redirectUrlString);
		RedirectView redirectView = new RedirectView();

		String redirectUrl = redirectUrlString;
		if (!redirectUrlString.startsWith("http")) {
			redirectUrl = "http://" + redirectUrlString;
		}
		redirectView.setUrl(redirectUrl);
		return redirectView;
	}
	
	
	@RequestMapping(value = "/customurl", method = RequestMethod.POST, consumes = { "application/json" })
	@ResponseBody
	public ResponseEntity<String> customURL(@RequestBody final ShortenRequest shortenRequest,
			HttpServletRequest request) throws Exception {
		LOGGER.info("Input url to custom shorten urls : " + shortenRequest.getUrl());
		String longUrl = shortenRequest.getUrl();
		StringBuilder stringbuilder = new StringBuilder();
		
		if (URLValidator.INSTANCE.validateURL(longUrl)) {
			stringbuilder.append("{\n");
			stringbuilder.append("\"customurl\":[");
			for (int index = 0; index < 5; index++) {
				String localURL = request.getRequestURL().toString();
				String shortenedUrl = urlConverterService.shortenURL(localURL, shortenRequest.getUrl());
				LOGGER.info("Shortened url to: " + shortenedUrl);
				stringbuilder.append("\"").append(shortenedUrl).append("\"");
				if(index!=4) stringbuilder.append(",");
			}
			stringbuilder.append("]");
			stringbuilder.append("\n}");
			return new ResponseEntity<>(stringbuilder.toString(), HttpStatus.OK);
		} else {

			String ss = "{ \"error\" : \"400\",\"error_description\" : \"Invalid input URL.\" }";
			return new ResponseEntity<>(ss, HttpStatus.BAD_REQUEST);
		}
	} 
}

class ShortenRequest {
	private String url;

	@JsonCreator
	public ShortenRequest() {

	}

	@JsonCreator
	public ShortenRequest(@JsonProperty("url") String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
