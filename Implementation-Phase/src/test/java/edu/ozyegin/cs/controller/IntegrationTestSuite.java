package edu.ozyegin.cs.controller;

import edu.ozyegin.cs.HomeworkMySQLContainer;
import edu.ozyegin.cs.Pair;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;
import java.util.*;
import java.util.regex.Pattern;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestSuite {

    protected final String url = "http://localhost:";
    private static String[] tableNames;

    @ClassRule
    public static MySQLContainer mySQLContainer = HomeworkMySQLContainer.getInstance();

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;


    protected JdbcTemplate jdbcTemplate;
    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        String basePackage = "edu.ozyegin.cs.entity";

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

        ArrayList<String> entities = new ArrayList<>();

        for (BeanDefinition definition : scanner.findCandidateComponents(basePackage)) {
            entities.add(
                    Objects.requireNonNull(definition.getBeanClassName())
                            .replace(String.format("%s.", basePackage), "")
            );
        }

        tableNames = entities.toArray(new String[0]);
    }

    @Before
    public void before() throws Exception {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, tableNames);
    }

    @After
    public void after() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, tableNames);
    }

    public <T> void assertTwoListEqual(Collection<T> expectedList, Collection<T> actualList) {
        Assert.assertEquals(expectedList.size(), actualList.size());
        Set<T> set = new HashSet<>(actualList);
        for (T t : expectedList) {
            Assert.assertTrue(set.contains(t));
        }
        Set<T> set2 = new HashSet<>(expectedList);
        for (T t : actualList) {
            Assert.assertTrue(set2.contains(t));
        }
    }

    protected int random(int limit) {
        return (int) (Math.random() * limit);
    }

    private <T> T genericMethod(String path, Class<T> responseClass, Object body, List<Pair<String, Object>> queryParams, List<Pair<String, String>> headerParams, HttpMethod type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        for (Map.Entry<String, String> params : headerParams) {
            headers.set(params.getKey(), params.getValue());
        }
        headers.set("jwt", "dummy_token");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + port + "/" + path);
        for (Map.Entry<String, Object> params : queryParams) {
            builder = builder.queryParam(params.getKey(), params.getValue());
        }

        ResponseEntity<T> responseEntity = testRestTemplate.exchange(builder.build().toUri(), type, new HttpEntity<>(body, headers), responseClass);
        //Assert.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        return responseEntity.getBody();
    }

    public <T> T getMethod(String path, Class<T> responseClass) {
        return genericMethod(path, responseClass, null, new ArrayList<>(), new ArrayList<>(), HttpMethod.GET);
    }

    public <T> T getMethod(String path, Class<T> responseClass, List<Pair<String, Object>> queryParams, List<Pair<String, String>> headerParams) {
        return genericMethod(path, responseClass, null, queryParams, headerParams, HttpMethod.GET);
    }

    public <T> T postMethod(String path, Class<T> responseClass, Object body) {
        return genericMethod(path, responseClass, body, new ArrayList<>(), new ArrayList<>(), HttpMethod.POST);
    }

    public <T> T postMethod(String path, Class<T> responseClass, Object body, List<Pair<String, Object>> queryParams, List<Pair<String, String>> headerParams) {
        return genericMethod(path, responseClass, body, queryParams, headerParams, HttpMethod.POST);
    }

    public <T> T putMethod(String path, Class<T> responseClass, Object body, List<Pair<String, Object>> queryParams, List<Pair<String, String>> headerParams) {
        return genericMethod(path, responseClass, body, queryParams, headerParams, HttpMethod.PUT);
    }

    public <T> T deleteMethod(String path, Class<T> responseClass, List<Pair<String, Object>> queryParams, List<Pair<String, String>> headerParams) {
        return genericMethod(path, responseClass, null, queryParams, headerParams, HttpMethod.DELETE);
    }
}
