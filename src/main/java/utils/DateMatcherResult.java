package utils;

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class DateMatcherResult {
    private String type;
    private LinkedHashSet<String> result;

    public DateMatcherResult() {
    }

    public DateMatcherResult(String type, LinkedHashSet<String> result) {
        this.type = type;
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LinkedHashSet<String> getResult() {
        return result;
    }

    public void setResult(LinkedHashSet<String> result) {
        this.result = result;
    }
}
