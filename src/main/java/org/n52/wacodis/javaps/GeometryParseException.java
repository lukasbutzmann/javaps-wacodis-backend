package org.n52.wacodis.javaps;

/**
 * An exception that will be thrown if a geometry can not be parsed out of the String that includes the geometry.
 *
 * @author @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class GeometryParseException extends Exception {

    public GeometryParseException() {
    }

    public GeometryParseException(String message, String expectedFormat) {
        super(String.format("%s\nExpected format: %s", message, expectedFormat));
    }

    public GeometryParseException(String message, String expectedFormat, Throwable cause) {
        super(String.format("%s\nExpected format: %s", message, expectedFormat), cause);
    }

    public GeometryParseException(String message) {
        super(message);
    }

    public GeometryParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeometryParseException(Throwable cause) {
        super(cause);
    }
}
