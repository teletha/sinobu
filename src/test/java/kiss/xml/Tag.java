/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 2012/11/17 10:14:57
 */
public class Tag {

    private static final Map<String, Tag> tags = new HashMap<String, Tag>();

    private static final Tag defaultAncestor;
    static {
        defaultAncestor = new Tag("BODY");
        tags.put(defaultAncestor.tagName, defaultAncestor);
    }

    private String tagName;

    private boolean knownTag = false; // if pre-defined or auto-created

    private boolean isBlock = true; // block or inline

    private boolean formatAsBlock = true;

    private boolean canContainBlock = true; // Can this tag hold block level tags?

    private boolean canContainInline = true; // only pcdata if not

    private boolean optionalClosing = false; // If tag is open, and another seen, close previous tag

    private boolean empty = false; // can hold nothing; e.g. img

    private boolean selfClosing = false; // can self close (<foo />). used for unknown tags that
                                         // self close, without forcing them as empty.

    private boolean preserveWhitespace = false; // for pre, textarea, script etc

    private List<Tag> ancestors; // elements must be a descendant of one of these ancestors

    private List<Tag> excludes = Collections.emptyList(); // cannot contain these tags

    private List<Tag> ignoreEndTags = Collections.emptyList(); // ignore these end tags

    private boolean directDescendant; // if true, must directly descend from one of the ancestors

    private boolean limitChildren; // if true, only contain children that've registered parents

    private Tag(String tagName) {
        this.tagName = tagName.toLowerCase();
    }

    public String getName() {
        return tagName;
    }

    /**
     * Get a Tag by name. If not previously defined (unknown), returns a new generic tag, that can
     * do anything.
     * <p>
     * Pre-defined tags (P, DIV etc) will be ==, but unknown tags are not registered and will only
     * .equals().
     * 
     * @param tagName Name of tag, e.g. "p". Case insensitive.
     * @return The tag, either defined or new generic.
     */
    public static Tag valueOf(String tagName) {
        tagName = tagName.trim().toLowerCase();

        synchronized (tags) {
            Tag tag = tags.get(tagName);
            if (tag == null) {
                // not defined: create default; go anywhere, do anything! (incl be inside a <p>)
                tag = new Tag(tagName);
                tag.setAncestor(defaultAncestor.tagName);
                tag.setExcludes();
                tag.isBlock = false;
                tag.canContainBlock = true;
            }
            return tag;
        }
    }

    /**
     * Test if this tag, the prospective parent, can accept the proposed child.
     * 
     * @param child potential child tag.
     * @return true if this can contain child.
     */
    boolean canContain(Tag child) {
        if (child.isBlock && !this.canContainBlock) return false;

        if (!child.isBlock && !this.canContainInline) // not block == inline
            return false;

        if (this.optionalClosing && this.equals(child)) return false;

        if (this.empty || this.isData()) return false;

        // don't allow children to contain their parent (directly)
        if (this.requiresSpecificParent() && this.getImplicitParent().equals(child)) return false;

        // confirm limited children
        if (limitChildren) {
            for (Tag childParent : child.ancestors) {
                if (childParent.equals(this)) return true;
            }
            return false;
        }

        // exclude children
        if (!excludes.isEmpty()) {
            for (Tag excluded : excludes) {
                if (child.equals(excluded)) return false;
            }
        }

        return true;
    }

    /**
     * Gets if this tag is a data only tag.
     * 
     * @return if this tag is a data only tag
     */
    public boolean isData() {
        return !canContainInline && !isEmpty();
    }

    /**
     * Get if this is an empty tag
     * 
     * @return if this is an emtpy tag
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Get if this is a pre-defined tag, or was auto created on parsing.
     * 
     * @return if a known tag
     */
    public boolean isKnownTag() {
        return knownTag;
    }

    Tag getImplicitParent() {
        return (!ancestors.isEmpty()) ? ancestors.get(0) : null;
    }

    boolean requiresSpecificParent() {
        return directDescendant;
    }

    boolean isValidParent(Tag child) {
        return isValidAncestor(child);
    }

    boolean isValidAncestor(Tag child) {
        if (child.ancestors.isEmpty()) return true; // HTML tag

        for (int i = 0; i < child.ancestors.size(); i++)
            if (this.equals(child.ancestors.get(i))) return true;

        return false;
    }

    boolean isIgnorableEndTag(Tag child) {
        for (Tag endTag : ignoreEndTags) {
            if (child.equals(endTag)) return true;
        }
        return false;
    }

    // internal static initialisers:

    static {
        // prepped from http://www.w3.org/TR/REC-html40/sgml/dtd.html#inline
        // tags are set here in uppercase for legibility, but internally held as lowercase.
        // TODO[must]: incorporate html 5 as appropriate

        // document
        createBlock("HTML").setAncestor(); // specific includes not impl
        createBlock("HEAD").setParent("HTML").setLimitChildren();
        createBlock("BODY").setAncestor("HTML"); // specific includes not impl
        createBlock("FRAMESET").setAncestor("HTML");

        // head
        // all ancestors set to (head, body): so implicitly create head, but allow in body
        createBlock("SCRIPT").setAncestor("HEAD", "BODY").setContainDataOnly();
        createBlock("NOSCRIPT").setAncestor("HEAD", "BODY");
        createBlock("STYLE").setAncestor("HEAD", "BODY").setContainDataOnly();
        createBlock("META").setAncestor("HEAD", "BODY").setEmpty();
        createBlock("LINK").setAncestor("HEAD", "BODY").setEmpty(); // only within head
        createInline("OBJECT").setAncestor("HEAD", "BODY"); // flow (block/inline) or param
        createBlock("TITLE").setAncestor("HEAD", "BODY").setContainDataOnly().setFormatAsInline();
        createInline("BASE").setAncestor("HEAD", "BODY").setEmpty();

        createBlock("FRAME").setParent("FRAMESET").setEmpty();
        createBlock("NOFRAMES").setParent("FRAMESET").setContainDataOnly();

        // html5 sections
        createBlock("SECTION");
        createBlock("NAV");
        createBlock("ASIDE");
        createBlock("HGROUP").setLimitChildren(); // limited to h1 - h6
        createBlock("HEADER").setExcludes("HEADER", "FOOTER");
        createBlock("FOOTER").setExcludes("HEADER", "FOOTER");

        // fontstyle
        createInline("FONT").setOptionalClosing().setCanContainBlock().setFormatAsInline();
        createInline("TT");
        createInline("I");
        createInline("B");
        createInline("U");
        createInline("BIG");
        createInline("SMALL");

        // phrase
        createInline("EM");
        createInline("STRONG");
        createInline("DFN").setOptionalClosing();
        createInline("CODE");
        createInline("SAMP");
        createInline("KBD");
        createInline("VAR");
        createInline("CITE");
        createInline("ABBR");
        createInline("TIME").setOptionalClosing();
        createInline("ACRONYM");
        createInline("MARK");

        // ruby
        createInline("RUBY");
        createInline("RT").setParent("RUBY").setExcludes("RT", "RP");
        createInline("RP").setParent("RUBY").setExcludes("RT", "RP");

        // special
        createInline("A").setOptionalClosing().setCanContainBlock().setFormatAsInline(); // cannot
                                                                                         // contain
                                                                                         // self
        createInline("IMG").setEmpty().setAncestor("BODY", "NOSCRIPT"); // noscript so an image can
                                                                        // be in
                                                                        // html->head->noscript
        createInline("BR").setEmpty();
        createInline("WBR").setEmpty();
        createInline("MAP"); // map is defined as inline, but can hold block (what?) or area. Seldom
                             // used so NBD.
        createInline("Q");
        createInline("SUB");
        createInline("SUP");
        createInline("BDO");
        createInline("IFRAME").setOptionalClosing();
        createInline("EMBED").setEmpty();

        // things past this point aren't really blocks or inline. I'm using them because they can
        // hold block or inline,
        // but per the spec, only specific elements can hold this. if this becomes a real-world
        // parsing problem,
        // will need to have another non block/inline type, and explicit include & exclude rules.
        // should be right though

        // block
        createInline("SPAN").setCanContainBlock().setFormatAsInline(); // spec is phrasing only,
                                                                       // practise is block
        createBlock("P").setContainInlineOnly(); // emasculated block?
        createBlock("H1").setAncestor("BODY", "HGROUP")
                .setExcludes("HGROUP", "H1", "H2", "H3", "H4", "H5", "H6")
                .setFormatAsInline();
        createBlock("H2").setAncestor("BODY", "HGROUP")
                .setExcludes("HGROUP", "H1", "H2", "H3", "H4", "H5", "H6")
                .setFormatAsInline();
        createBlock("H3").setAncestor("BODY", "HGROUP")
                .setExcludes("HGROUP", "H1", "H2", "H3", "H4", "H5", "H6")
                .setFormatAsInline();
        createBlock("H4").setAncestor("BODY", "HGROUP")
                .setExcludes("HGROUP", "H1", "H2", "H3", "H4", "H5", "H6")
                .setFormatAsInline();
        createBlock("H5").setAncestor("BODY", "HGROUP")
                .setExcludes("HGROUP", "H1", "H2", "H3", "H4", "H5", "H6")
                .setFormatAsInline();
        createBlock("H6").setAncestor("BODY", "HGROUP")
                .setExcludes("HGROUP", "H1", "H2", "H3", "H4", "H5", "H6")
                .setFormatAsInline();
        createBlock("UL");
        createBlock("OL");
        createBlock("PRE").setContainInlineOnly().setPreserveWhitespace();
        createBlock("DIV");
        createBlock("BLOCKQUOTE");
        createBlock("HR").setEmpty();
        createBlock("ADDRESS").setContainInlineOnly();
        createBlock("FIGURE");
        createBlock("FIGCAPTION").setAncestor("FIGURE");

        // formctrl
        createBlock("FORM").setOptionalClosing(); // can't contain self
        createInline("INPUT").setAncestor("FORM").setEmpty();
        createInline("SELECT").setAncestor("FORM"); // just contain optgroup or option
        createInline("TEXTAREA").setAncestor("FORM").setContainDataOnly();
        createInline("LABEL").setAncestor("FORM").setOptionalClosing(); // not self
        createInline("BUTTON").setAncestor("FORM"); // bunch of excludes not defined
        createInline("OPTGROUP").setParent("SELECT"); // only contain option
        createInline("OPTION").setParent("SELECT", "OPTGROUP", "DATALIST").setOptionalClosing();
        createBlock("FIELDSET").setAncestor("FORM");
        createInline("LEGEND").setAncestor("FIELDSET");

        // html5 form ctrl, not specced to have to be in forms
        createInline("DATALIST");
        createInline("KEYGEN").setEmpty();
        createInline("OUTPUT");
        createInline("PROGRESS").setOptionalClosing();
        createInline("METER").setOptionalClosing();

        // other
        createInline("AREA").setAncestor("MAP").setEmpty(); // not an inline per-se
        createInline("PARAM").setParent("OBJECT").setEmpty();
        createBlock("INS"); // only within body
        createBlock("DEL"); // only within body

        // definition lists. per spec, dt and dd are inline and must directly descend from dl.
        // However in practise
        // these are all used as blocks and dl need only be an ancestor
        createBlock("DL").setOptionalClosing(); // can't nest
        createBlock("DT").setAncestor("DL").setExcludes("DL", "DD").setOptionalClosing(); // only
                                                                                          // within
                                                                                          // DL.
        createBlock("DD").setAncestor("DL").setExcludes("DL", "DT").setOptionalClosing(); // only
                                                                                          // within
                                                                                          // DL.

        createBlock("LI").setAncestor("UL", "OL").setOptionalClosing().setFormatAsInline(); // only
                                                                                            // within
                                                                                            // OL or
                                                                                            // UL.

        // tables
        createBlock("TABLE").setOptionalClosing()
                .setIgnoreEnd("BODY", "CAPTION", "COL", "COLGROUP", "HTML", "TBODY", "TD", "TFOO", "TH", "THEAD", "TR"); // specific
                                                                                                                         // list
                                                                                                                         // of
                                                                                                                         // only
                                                                                                                         // includes
                                                                                                                         // (tr,
                                                                                                                         // td,
                                                                                                                         // thead
                                                                                                                         // etc)
                                                                                                                         // not
                                                                                                                         // implemented
        createBlock("CAPTION").setParent("TABLE")
                .setExcludes("THEAD", "TFOOT", "TBODY", "COLGROUP", "COL", "TR", "TH", "TD")
                .setOptionalClosing()
                .setIgnoreEnd("BODY", "COL", "COLGROUP", "HTML", "TBODY", "TD", "TFOOT", "TH", "THEAD", "TR");
        createBlock("THEAD").setParent("TABLE")
                .setLimitChildren()
                .setOptionalClosing()
                .setIgnoreEnd("BODY", "CAPTION", "COL", "COLGROUP", "HTML", "TD", "TH", "TR"); // just
                                                                                               // TR
        createBlock("TFOOT").setParent("TABLE")
                .setLimitChildren()
                .setOptionalClosing()
                .setIgnoreEnd("BODY", "CAPTION", "COL", "COLGROUP", "HTML", "TD", "TH", "TR"); // just
                                                                                               // TR
        createBlock("TBODY").setParent("TABLE")
                .setLimitChildren()
                .setOptionalClosing()
                .setIgnoreEnd("BODY", "CAPTION", "COL", "COLGROUP", "HTML", "TD", "TH", "TR"); // optional
                                                                                               // /
                                                                                               // implicit
                                                                                               // open
                                                                                               // too.
                                                                                               // just
                                                                                               // TR
        createBlock("COLGROUP").setParent("TABLE").setLimitChildren().setOptionalClosing().setIgnoreEnd("COL"); // just
                                                                                                                // COL
        createBlock("COL").setParent("COLGROUP").setEmpty();
        createBlock("TR").setParent("TBODY", "THEAD", "TFOOT", "TABLE")
                .setLimitChildren()
                .setOptionalClosing()
                .setIgnoreEnd("BODY", "CAPTION", "COL", "COLGROUP", "HTML", "TD", "TH"); // just TH,
                                                                                         // TD
        createBlock("TH").setParent("TR")
                .setExcludes("THEAD", "TFOOT", "TBODY", "COLGROUP", "COL", "TR", "TH", "TD")
                .setOptionalClosing()
                .setIgnoreEnd("BODY", "CAPTION", "COL", "COLGROUP", "HTML")
                .setFormatAsInline();
        createBlock("TD").setParent("TR")
                .setExcludes("THEAD", "TFOOT", "TBODY", "COLGROUP", "COL", "TR", "TH", "TD")
                .setOptionalClosing()
                .setIgnoreEnd("BODY", "CAPTION", "COL", "COLGROUP", "HTML")
                .setFormatAsInline();

        // html5 media
        createBlock("VIDEO").setExcludes("VIDEO", "AUDIO");
        createBlock("AUDIO").setExcludes("VIDEO", "AUDIO");
        createInline("SOURCE").setParent("VIDEO", "AUDIO").setEmpty();
        createInline("TRACK").setParent("VIDEO", "AUDIO").setEmpty();
        createBlock("CANVAS");

        // html5 interactive
        createBlock("DETAILS");
        createInline("SUMMARY").setParent("DETAILS");
        createInline("COMMAND").setEmpty();
        createBlock("MENU");
        createInline("DEVICE").setEmpty();
    }

    private static Tag createBlock(String tagName) {
        return register(new Tag(tagName));
    }

    private static Tag createInline(String tagName) {
        Tag inline = new Tag(tagName);
        inline.isBlock = false;
        inline.canContainBlock = false;
        inline.formatAsBlock = false;
        return register(inline);
    }

    private static Tag register(Tag tag) {
        tag.setAncestor(defaultAncestor.tagName);
        tag.setKnownTag();
        synchronized (tags) {
            tags.put(tag.tagName, tag);
        }
        return tag;
    }

    private Tag setCanContainBlock() {
        canContainBlock = true;
        return this;
    }

    private Tag setContainInlineOnly() {
        canContainBlock = false;
        canContainInline = true;
        formatAsBlock = false;
        return this;
    }

    private Tag setFormatAsInline() {
        formatAsBlock = false;
        return this;
    }

    private Tag setContainDataOnly() {
        canContainBlock = false;
        canContainInline = false;
        preserveWhitespace = true;
        return this;
    }

    private Tag setEmpty() {
        canContainBlock = false;
        canContainInline = false;
        empty = true;
        return this;
    }

    private Tag setOptionalClosing() {
        optionalClosing = true;
        return this;
    }

    private Tag setPreserveWhitespace() {
        preserveWhitespace = true;
        return this;
    }

    private Tag setAncestor(String... tagNames) {
        if (tagNames == null || tagNames.length == 0) {
            ancestors = Collections.emptyList();
        } else {
            ancestors = new ArrayList<Tag>(tagNames.length);
            for (String name : tagNames) {
                ancestors.add(Tag.valueOf(name));
            }
        }
        return this;
    }

    private Tag setExcludes(String... tagNames) {
        if (tagNames == null || tagNames.length == 0) {
            excludes = Collections.emptyList();
        } else {
            excludes = new ArrayList<Tag>(tagNames.length);
            for (String name : tagNames) {
                excludes.add(Tag.valueOf(name));
            }
        }
        return this;
    }

    private Tag setIgnoreEnd(String... tagNames) {
        if (tagNames == null || tagNames.length == 0) {
            ignoreEndTags = Collections.emptyList();
        } else {
            ignoreEndTags = new ArrayList<Tag>(tagNames.length);
            for (String name : tagNames) {
                ignoreEndTags.add(Tag.valueOf(name));
            }
        }
        return this;
    }

    private Tag setParent(String... tagNames) {
        directDescendant = true;
        setAncestor(tagNames);
        return this;
    }

    private Tag setLimitChildren() {
        limitChildren = true;
        return this;
    }

    Tag setSelfClosing() {
        selfClosing = true;
        return this;
    }

    private Tag setKnownTag() {
        knownTag = true;
        return this;
    }
}
