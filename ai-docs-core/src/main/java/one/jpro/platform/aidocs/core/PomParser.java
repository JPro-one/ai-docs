package one.jpro.platform.aidocs.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;

/**
 * Parses Maven POM files to extract metadata like name, description, URL, and license.
 * Uses the built-in {@code javax.xml} APIs — no external dependencies required.
 */
public class PomParser {

    /**
     * Parses a POM file and extracts metadata.
     *
     * @param pomFile path to the POM XML file
     * @return extracted metadata (fields may be null if not present in the POM)
     */
    public static PomMetadata parse(Path pomFile) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            var builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile.toFile());
            Element root = doc.getDocumentElement();

            String name = getDirectChildText(root, "name");
            String description = getDirectChildText(root, "description");
            String url = getDirectChildText(root, "url");
            String scmUrl = extractScmUrl(root);
            String license = extractLicenseName(root);

            return new PomMetadata(name, description, url, scmUrl, license);
        } catch (Exception e) {
            return new PomMetadata(null, null, null, null, null);
        }
    }

    /**
     * Extracts parent coordinates from a POM file.
     *
     * @param pomFile path to the POM XML file
     * @return a {@code String[]{groupId, artifactId, version}} or {@code null} if no parent is declared
     */
    public static String[] parseParent(Path pomFile) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            var builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile.toFile());
            Element root = doc.getDocumentElement();

            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element el && el.getTagName().equals("parent")) {
                    String groupId = getDirectChildText(el, "groupId");
                    String artifactId = getDirectChildText(el, "artifactId");
                    String version = getDirectChildText(el, "version");
                    if (groupId != null && artifactId != null && version != null) {
                        return new String[]{groupId, artifactId, version};
                    }
                    return null;
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return null;
    }

    /**
     * Gets the text content of a direct child element of the given parent, with all
     * whitespace runs (including newlines from indented POMs) collapsed to single spaces.
     * Returns null if the element doesn't exist or has empty text.
     */
    private static String getDirectChildText(Element parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element el && el.getTagName().equals(tagName)) {
                String text = el.getTextContent().strip().replaceAll("\\s+", " ");
                return text.isEmpty() ? null : text;
            }
        }
        return null;
    }

    /**
     * Extracts the browsable URL from {@code <scm><url>}.
     */
    private static String extractScmUrl(Element root) {
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element el && el.getTagName().equals("scm")) {
                return getDirectChildText(el, "url");
            }
        }
        return null;
    }

    /**
     * Extracts the name of the first license from {@code <licenses><license><name>}.
     */
    private static String extractLicenseName(Element root) {
        NodeList licensesNodes = root.getChildNodes();
        for (int i = 0; i < licensesNodes.getLength(); i++) {
            if (licensesNodes.item(i) instanceof Element el && el.getTagName().equals("licenses")) {
                NodeList licenseNodes = el.getElementsByTagName("license");
                if (licenseNodes.getLength() > 0) {
                    Element license = (Element) licenseNodes.item(0);
                    NodeList nameNodes = license.getElementsByTagName("name");
                    if (nameNodes.getLength() > 0) {
                        String text = nameNodes.item(0).getTextContent().strip();
                        return text.isEmpty() ? null : text;
                    }
                }
                break;
            }
        }
        return null;
    }
}
