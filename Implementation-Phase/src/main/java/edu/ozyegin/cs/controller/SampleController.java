package edu.ozyegin.cs.controller;

import edu.ozyegin.cs.entity.Sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.*;

@RestController
@RequestMapping
@CrossOrigin
public class SampleController {
    @Autowired
    private PlatformTransactionManager transactionManager;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    final int batchSize = 10;

    final String createPS = "INSERT INTO Sample (name, data, value) VALUES(?,?,?)";
    final String selectPS = "SELECT * FROM Sample";

    /**
     * Returns a JSON object with "Hello World" message.<br/>
     * Response outline:<br/>
     * <pre>
     *  {
     *      "message": "Hello World",
     *      "status": true
     *  }
     * </pre>
     * */
    @RequestMapping(value = "/sample", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity helloWorld() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("message", "Hello World");
            response.put("status", true);
        } catch (Exception ex) {
            response.put("status", false);
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Returns a JSON object that echoes received message.<br/>
     * Response outline:<br/>
     * <pre>
     *  {
     *      "echo": " . . . "
     *  }
     * </pre>
     * */
    @RequestMapping(value = "/sample/echo", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity echo(@RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("echo", payload.get("message"));

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
    }

    /**
     * Returns a JSON object with an array of all Samples present in DB.<br/>
     * Response outline:<br/>
     * <pre>
     *  {
     *      "samples": [
     *          { "id": 0, "name": "Some Name", ... },
     *          { "id": 1, "name": "Something", ... },
     *          ...
     *      ],
     *      "status": true
     *  }
     * </pre>
     * */
    @RequestMapping(value = "/sample/entities", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity entities() {
        try {
            List<Sample> data = Objects.requireNonNull(jdbcTemplate).query(selectPS, new BeanPropertyRowMapper<>(Sample.class));

            Map<String, Object> response = new HashMap<>();
            response.put("samples", data);
            response.put("status", true);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
    }

    /**
     * Generates a 'Sample' object to from POST data and inserts into DB.<br/>
     * Returns <code>success</code> status in response data. <code>true</code> if successful, <code>false</code> otherwise<br/>
     * Response outline:<br/>
     * <pre>
     *  {
     *      "success": true
     *  }
     * </pre>
     * */
    @RequestMapping(value = "/sample/create", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity create(@RequestBody Map<String, Object>[] payload) {
        // prepare data for usage
        List<Sample> samples = new ArrayList<>();
        for (Map<String, Object> entity : payload) {
            Sample sample = new Sample();
            sample.setName((String) entity.get("name"));
            sample.setData((String) entity.get("data"));
            sample.setValue((int) entity.get("value"));

            samples.add(sample);
        }

        // init Transaction Manager
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        // create response's structure
        Map<String, Object> response = new HashMap<>();

        try {
            // INSERT INTO Samples using a PREPARED STATEMENT
            Objects.requireNonNull(jdbcTemplate).batchUpdate(createPS, samples, batchSize,
                    (ps, sample) -> {
                        ps.setString(1, sample.getName());
                        ps.setString(2, sample.getData());
                        ps.setInt(3, sample.getValue());
                    });

            // commit changes to database
            transactionManager.commit(txStatus);

            response.put("success", true);  // prepare data to respond with
        } catch (Exception exception) {
            // revert changes planned
            transactionManager.rollback(txStatus);

            // prepare data to respond with
            response.put("success", false);
            response.put("message", "Failed inserting Samples");
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
