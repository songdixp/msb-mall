/*
package com.msb.mall.search;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.mall.search.config.MallElasticSearchConfiguration;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
class MallSearchApplicationTests {
	@Autowired
	RestHighLevelClient client;
	@Test
	void contextLoads() {
	}
	// @Test
	void getClient(){
		System.out.println("----->"+ client);
	}

*
	 * 保存索引操作


	// @Test
	void saveIndex() throws IOException {
		IndexRequest request = new IndexRequest("sason");
		request.id("1");
		User user = new User();
		user.setAge(18);
		user.setName("sason");
		user.setGender("男");
		ObjectMapper obj = new ObjectMapper();
		String json = obj.writeValueAsString(user);
		request.source(json, XContentType.JSON);
		// 点击执行按钮
		IndexResponse index = client.index(request, MallElasticSearchConfiguration.COMMON_OPTIONS);
		System.out.println("index = " + index);
	}

*
	 * 搜索API


	// @Test
void searchIndex() throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		// 指定索引
		searchRequest.indices("blank");
		SearchSourceBuilder builder = new SearchSourceBuilder();
		builder.size(5);
		searchRequest.source(builder);
		SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
		System.out.println("response = " + response);
	}

	// @Test
	void jsonParse(){
		String str = "{\"age\":\"24\",\"name\":\"cool_summer_moon\"}";
		Map<String, Object> jsonObject = JSONObject.parseObject(str, Map.class);
		System.out.println("jsonObject = " + jsonObject+"class-->"+jsonObject.getClass());
Object age = jsonObject.get("age");
		System.out.println("age = " + age);
		String name = jsonObject.getObject("name", String.class);
		System.out.println("name = " + name+"class-->"+name.getClass());

		String s = jsonObject.toJSONString();
		System.out.println("s = " + s+" clazz:"+s.getClass());


	}

	@Data
	static class User{
		private String name;
		private Integer age;
		private String gender;
	}
}
*/
