package sample.context.orm;

import static java.util.regex.Pattern.*;

import java.util.regex.*;

import org.springframework.util.*;

/**
 * An ORM-Related utility.
 */
public abstract class OrmUtils {

    private static final String IDENTIFIER = "[._[\\P{Z}&&\\P{Cc}&&\\P{Cf}&&\\P{P}]]+";
    private static final String IDENTIFIER_GROUP = String.format("(%s)", IDENTIFIER);
    private static final Pattern COUNT_MATCH;
    private static final int VARIABLE_NAME_GROUP_INDEX = 4;
    private static final String SIMPLE_COUNT_VALUE = "$2";
    private static final String COMPLEX_COUNT_VALUE = "$3$6";
    private static final String COUNT_REPLACEMENT_TEMPLATE = "select count(%s) $5$6$7";
    private static final String ORDER_BY_PART = "(?iu)\\s+order\\s+by\\s+.*$";

    static {
        StringBuilder builder = new StringBuilder();
        builder.append("(select\\s+((distinct )?(.+?)?)\\s+)?(from\\s+");
        builder.append(IDENTIFIER);
        builder.append("(?:\\s+as)?\\s+)");
        builder.append(IDENTIFIER_GROUP);
        builder.append("(.*)");
        COUNT_MATCH = compile(builder.toString(), CASE_INSENSITIVE);
    }
    
    public static String createCountQueryFor(String originalQuery) {
        Assert.hasText(originalQuery, "OriginalQuery must not be null or empty!");
        Matcher matcher = COUNT_MATCH.matcher(originalQuery);
        String variable = matcher.matches() ? matcher.group(VARIABLE_NAME_GROUP_INDEX) : null;
        boolean useVariable = variable != null && StringUtils.hasText(variable) && !variable.startsWith("new")
                && !variable.startsWith("count(") && !variable.contains(",");

        String replacement = useVariable ? SIMPLE_COUNT_VALUE : COMPLEX_COUNT_VALUE;
        String countQuery = matcher.replaceFirst(String.format(COUNT_REPLACEMENT_TEMPLATE, replacement));
        return countQuery.replaceFirst(ORDER_BY_PART, "");
    }

    
}
