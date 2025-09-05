package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.ebean.Finder;
import io.ebean.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = OperationLog.class)
@ToString
public class OperationLog extends Model {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String STR_OPERATION_LOGGER_SYSTEM = "SYSTEM";

    public static enum CHANNEL_TYPE {
        FRONTEND("FRONTEND"), BACKEND("BACKEND"), UNKNOWN("UNKNOWN");

        private String value = "";
        private CHANNEL_TYPE(String value) {this.value = value;}

//        public static CHANNEL_TYPE valueOf(String value) {
//            if ("FRONTEND".equals(value)) return FRONTEND;
//            else if ("BACKEND".equals(value)) return BACKEND;
//            return UNKNOWN;
//        }

        public String value() {
            return this.value;
        }
    };

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String logger;

    private String logKey;

    private String channel;

    private Long userId;

    private String dateTime;

    private Long timestamp;

    private String ip;

    private String routePattern;

    private String actionMethod;

    private String controller;

    private String comment;

    public static Finder<Long, OperationLog> find = new Finder<Long, OperationLog>(OperationLog.class);

    public OperationLog(
            String logKey, CHANNEL_TYPE channel, Long userId, Long timestamp, String ip, String routePattern, String actionMethod, String controller, String comment
    ) {
        this.logger = STR_OPERATION_LOGGER_SYSTEM;
        this.logKey = logKey;
        this.channel = channel.value();
        this.userId = userId;
        this.timestamp = timestamp;
        this.dateTime = SDF.format(new Date(Long.parseLong(String.valueOf(this.timestamp))));
        this.ip = ip;
        this.routePattern = routePattern;
        this.actionMethod = actionMethod;
        this.controller = controller;
        this.comment = comment;
    }
}
