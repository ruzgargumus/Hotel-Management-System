package edu.ozyegin.cs.controller;

import java.util.*;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import edu.ozyegin.cs.entity.Sample;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

public class SampleControllerTest extends IntegrationTestSuite {
    private List<Sample> generateSamples(int size) {
        ArrayList<Sample> samples = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            samples.add(new Sample()
                    .name(RandomStringUtils.random(random(10), true, true))
                    .data(RandomStringUtils.random(random(10), true, true))
                    .value(random(100)));
        }
        return samples;
    }

    @Test
    public void helloWorld() throws Exception {
        HashMap response = getMethod("/sample", HashMap.class);
        Assert.assertEquals("Hello World", response.get("message"));
    }

    @Test
    public void echo() throws Exception {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("message", "I am doing the CS202 Project!");

        HashMap response = postMethod("/sample/echo", HashMap.class, payload);
        Assert.assertEquals(payload.get("message"), response.get("echo"));
    }

    @Test
    public void create1() throws Exception {
        List<Sample> samples = generateSamples(1);

        postMethod("/sample/create", String.class, samples);

        List<Sample> data = Objects.requireNonNull(jdbcTemplate)
                .query("SELECT * FROM Sample", new BeanPropertyRowMapper<>(Sample.class));

        assertTwoListEqual(samples, data);
    }

    @Test
    public void create42() throws Exception {
        List<Sample> samples = generateSamples(42);

        postMethod("/sample/create", String.class, samples);

        List<Sample> data = Objects.requireNonNull(jdbcTemplate)
                .query("SELECT * FROM Sample", new BeanPropertyRowMapper<>(Sample.class));

        assertTwoListEqual(samples, data);
    }

    @Test
    public void create3() throws Exception {
        List<Sample> samples = generateSamples(3);

        postMethod("/sample/create", String.class, samples);

        List<Sample> data = Objects.requireNonNull(jdbcTemplate)
                .query("SELECT * FROM Sample", new BeanPropertyRowMapper<>(Sample.class));

        assertTwoListEqual(samples, data);
    }

    @Test
    public void fetch3() throws Exception {
        List<Sample> samples = generateSamples(3);
        Objects.requireNonNull(jdbcTemplate)
                .batchUpdate(
                        "INSERT INTO Sample (name, data, value) VALUES(?,?,?)",
                        samples,
                        10,
                        (ps, sample) -> {
                            ps.setString(1, sample.getName());
                            ps.setString(2, sample.getData());
                            ps.setInt(3, sample.getValue());
                        }
                );

        Gson gson = new Gson(); // for object type conversion

        HashMap response = getMethod("/sample/entities", HashMap.class);
        for (int i = 0; i < samples.size(); i++) {
            HashMap<String, Object> aObj = ((List<HashMap>) response.get("samples")).get(i);

            // properly serialize HashMap object to Sample object
            Sample a = gson.fromJson(gson.toJsonTree(aObj), Sample.class);
            // get data reference
            Sample b = samples.get(i);

            assert a.equals(b);
        }
    }
}
