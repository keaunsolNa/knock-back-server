package org.knock.knock_back.component.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

/**
 * @author nks
 * @apiNote ElasticSearch 설정
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "org.knock.knock_back.*")
@ComponentScan(basePackages = {"org.knock.knock_back.*"})
public class ElasticsearchBaseConfig {

	@Value("${elasticsearch.host}")
	private String host;

	@Bean
	public RestClient getRestClient() {

		try {

			// Bonsai URL에서 id와 password 추출
			String sanitizedHost = host.startsWith("http") ? host : "https://" + host;
			URI uri = new URI(sanitizedHost);
			String userInfo = uri.getUserInfo();
			String host = uri.getHost();
			int port = uri.getPort() == -1 ? 443 : uri.getPort(); // 기본 포트 설정

			// 인증 정보가 있다면 Authorization 헤더 추가
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			if (userInfo != null && userInfo.contains(":")) {
				String[] credentials = userInfo.split(":");
				credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(credentials[0], credentials[1]));
			}

			return RestClient.builder(new HttpHost(host, port, "https"))
				.setHttpClientConfigCallback(httpClientBuilder -> {
					httpClientBuilder.disableAuthCaching();
					httpClientBuilder.setDefaultHeaders(List.of(
						new BasicHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, "application/json")
					));
					httpClientBuilder.addInterceptorLast((HttpResponseInterceptor)
						(response, context) -> response.addHeader("X-Elastic-Product", "Elasticsearch"));
					return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				}).build();

		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid Elasticsearch URI: " + host, e);
		}

	}

	@Bean
	public ElasticsearchTransport getElasticsearchTransport() {
		return new RestClientTransport(getRestClient(), new JacksonJsonpMapper());
	}

	@Bean
	public ElasticsearchClient getElasticsearchClient() {
		return new ElasticsearchClient(getElasticsearchTransport());
	}
}
