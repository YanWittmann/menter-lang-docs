package de.yanwittmann.menter.doc;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class DocumentationPage {

    private final File originFile;
    private DocumentationPage parent;
    private final List<DocumentationPage> subPages = new ArrayList<>();
    private String title;
    private String description;
    private String[] keywords;
    private Node content;

    public DocumentationPage(File originFile) {
        this.originFile = originFile;
    }

    void parseContent(Parser parser, Map<String, String> externalProperties) throws IOException {
        if (content == null) {
            content = parser.parse(
                    String.join("\n", replaceVariables(
                            FileUtils.readLines(originFile, StandardCharsets.UTF_8), externalProperties
                    ))
            );
        }
    }

    private final static Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s[^ ]+:\\s[^ ]+\\s}}");

    private List<String> replaceVariables(List<String> input, Map<String, String> externalProperties) {
        // embedded files: {{ file: menter-lang/src/main/resources/src/system.mtr }}

        final List<String> output = new ArrayList<>();

        for (String line : input) {
            while (true) {
                final String trimmedLine = line.trim();

                final Matcher matcher = VARIABLE_PATTERN.matcher(trimmedLine);

                if (!matcher.find()) break;

                final String variable = matcher.group();
                final String[] variableParts = variable.substring(3, variable.length() - 3).split(":", 2);
                final String variableType = variableParts[0].trim();
                final String variableName = variableParts[1].trim();

                if (variableType.equals("file")) {
                    final File file = new File(variableName);
                    if (!file.exists()) {
                        throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist!");
                    }
                    try {
                        final List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
                        line = line.replace(variable, String.join("\n", lines));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (variableType.equals("property")) {
                    if (!externalProperties.containsKey(variableName)) {
                        throw new IllegalArgumentException("Property " + variableName + " does not exist!");
                    }
                    line = line.replace(variable, externalProperties.get(variableName));

                } else {
                    throw new IllegalArgumentException("Unknown variable type " + variableType + "!");
                }
            }

            if (line.startsWith("> content.description: ")) {
                description = line.substring("> content.description: ".length());
                continue;
            } else if (line.startsWith("> content.keywords: ")) {
                keywords = line.substring("> content.keywords: ".length()).split(", ");
                continue;
            }

            output.add(line);
        }

        return output;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParent(DocumentationPage parent) {
        this.parent = parent;
    }

    public void addSubPage(DocumentationPage subPage) {
        subPage.setParent(this);
        subPages.add(subPage);
    }

    public DomContent renderPageContent(HtmlRenderer renderer) {
        final DivTag div = div();

        System.out.println("Rendering " + getOutFileName());

        for (Node child : content.getChildren()) {
            // div.with(rawHtml(renderer.render(child)));
            div.with(render(renderer, child));
        }

        return div;
    }

    public DomContent render(HtmlRenderer renderer, Object input) {
        if (input instanceof Node) {
            final Node node = (Node) input;

            final DomContent tag;

            if (node instanceof Paragraph) {
                tag = p();
            } else if (node instanceof Heading) {
                final int level = ((Heading) node).getLevel();
                if (level == 1) {
                    tag = h1();
                } else if (level == 2) {
                    tag = h2();
                } else if (level == 3) {
                    tag = h3();
                } else if (level == 4) {
                    tag = h4();
                } else if (level == 5) {
                    tag = h5();
                } else if (level == 6) {
                    tag = h6();
                } else {
                    tag = h1();
                }
            } else if (node instanceof Text) {
                tag = text(node.getChars().toString());
            } else if (node instanceof HardLineBreak) {
                tag = br();
            } else if (node instanceof SoftLineBreak) {
                tag = text(" ");
            } else if (node instanceof BulletList) {
                tag = ul();
            } else if (node instanceof BulletListItem) {
                tag = li();
            } else if (node instanceof OrderedList) {
                tag = ol();
            } else if (node instanceof OrderedListItem) {
                tag = li();
            } else if (node instanceof Link) {
                tag = a().withHref(((Link) node).getUrl().toString());
            } else if (node instanceof LinkRef) {
                throw new UnsupportedOperationException("LinkRef not supported, do not include spaces in links!");
            } else if (node instanceof Code) {
                tag = code();
            } else if (node instanceof Emphasis) {
                tag = em();
            } else if (node instanceof StrongEmphasis) {
                tag = strong();
            } else if (node instanceof BlockQuote) {
                tag = blockquote();
            } else if (node instanceof Image) {
                final String imageUrl = ((Image) node).getUrl().toString();
                final String alt = ((Image) node).getText().toString();

                if (imageUrl.startsWith("guide/")) {
                    final String guideImagePath = "img/" + imageUrl.substring("guide/".length());
                    tag = img().withSrc(guideImagePath).withAlt(alt);
                } else {
                    tag = img().withSrc(imageUrl).withAlt(alt);
                }
            } else if (node instanceof FencedCodeBlock) {
                final String codeBlockType = ((FencedCodeBlock) node).getInfo().toString();
                final List<String> codeBlockLines = ((FencedCodeBlock) node).getContentLines().stream()
                        .map(BasedSequence::toString)
                        .map(e -> e.replace("\n", ""))
                        .collect(Collectors.toList());

                // trim empty lines from start and end
                while (codeBlockLines.get(0).isEmpty()) codeBlockLines.remove(0);
                while (codeBlockLines.get(codeBlockLines.size() - 1).isEmpty())
                    codeBlockLines.remove(codeBlockLines.size() - 1);

                final String[] typeArguments = codeBlockType.split("---");
                final Map<String, String> typeArgumentsMap = Arrays.stream(typeArguments)
                        .map(e -> e.split("=", 2))
                        .collect(Collectors.toMap(e -> e[0], e -> e.length > 1 ? e[1] : ""));

                final boolean isStatic = typeArgumentsMap.containsKey("static");
                final String presetResult = typeArgumentsMap.getOrDefault("result", null);
                final String id = typeArgumentsMap.getOrDefault("id", null);
                final String after = typeArgumentsMap.getOrDefault("after", null);
                final String lang = typeArgumentsMap.getOrDefault("lang", null);

                final DivTag actualCodeboxTag = div().withClasses("codebox-container", "codebox-fake")
                        .attr("initialContent", String.join(":NEWLINE:", codeBlockLines))
                        .attr("interactive", !isStatic);

                if (id != null) actualCodeboxTag.attr("codebox-id", id);
                if (after != null) actualCodeboxTag.attr("after", after);
                if (presetResult != null) actualCodeboxTag.attr("result", presetResult);
                if (lang != null) actualCodeboxTag.attr("lang", lang);

                tag = actualCodeboxTag;
            } else {
                System.err.println("Unknown input: " + input.getClass().getSimpleName() + ", using default renderer");
                return rawHtml(renderer.render(node));
            }

            if (tag != null) {
                if (tag instanceof ContainerTag) {
                    for (Node child : node.getChildren()) {
                        ((ContainerTag<?>) tag).with(render(renderer, child));
                    }
                }

                return tag;
            }
        }

        return null;
    }

    public File getOutFile(File targetBaseDir) {
        return new File(targetBaseDir, getOutFileName());
    }

    public String getOutFileName() {
        return (parent != null ? parent.title.replace(" ", "_") + "_" : "") + originFile.getName().replace(".md", ".html");
    }

    public String getTitle() {
        return title;
    }

    public File getOriginFile() {
        return originFile;
    }

    public ATag renderSidebarItem() {
        return a(iff(parent != null, rawHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")), text(title))
                .withHref(getOutFileName())
                .withClass("sidebar-menu-item");
    }

    public String getDescription() {
        if (this.description == null) {
            return "➔ A functional programming language written in Java.";
        }
        if (this.description.length() > 140) {
            System.out.println("Description of " + title + " is too long! (" + this.description.length() + " chars)");
        }
        return description;
    }

    public String[] getKeywords() {
        if (this.keywords == null) {
            return new String[]{"functional", "programming", "language", "java"};
        }
        return keywords;
    }

    public List<String> getTitles() {
        final List<String> titles = new ArrayList<>();
        // iterate all lines and find all titles
        try (final BufferedReader reader = new BufferedReader(new FileReader(originFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    titles.add(line.replaceFirst("#+", "").trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return titles;
    }

    @Override
    public String toString() {
        return (parent != null ? parent.title + " >> " : "") + title;
    }

    public JSONObject toIndexObject() {
        final JSONObject object = new JSONObject();
        object.put("title", title);
        object.put("file", getOutFileName());
        object.put("description", getDescription());

        final List<String> extendedKeywords = new ArrayList<>(Arrays.asList(getKeywords()));
        extendedKeywords.addAll(getTitles());
        object.put("keywords", extendedKeywords);

        if (subPages.size() > 0) {
            final List<JSONObject> subPagesObjects = subPages.stream()
                    .map(DocumentationPage::toIndexObject)
                    .collect(Collectors.toList());
            object.put("subPages", subPagesObjects);
        }
        return object;
    }
}
