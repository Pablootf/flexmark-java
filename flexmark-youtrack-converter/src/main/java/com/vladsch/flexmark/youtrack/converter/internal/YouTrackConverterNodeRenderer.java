package com.vladsch.flexmark.youtrack.converter.internal;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.ast.util.TextCollectingVisitor;
import com.vladsch.flexmark.html.CustomNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.parser.ListOptions;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.html.Escaping;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.vladsch.flexmark.util.sequence.BasedSequence.NULL;

public class YouTrackConverterNodeRenderer implements NodeRenderer
        // , PhasedNodeRenderer
{
    private final ReferenceRepository referenceRepository;
    private final ListOptions listOptions;
    private int inBlockQuote = 0;
    private final boolean recheckUndefinedReferences;

    public YouTrackConverterNodeRenderer(DataHolder options) {
        this.referenceRepository = options.get(Parser.REFERENCES);
        recheckUndefinedReferences = HtmlRenderer.RECHECK_UNDEFINED_REFERENCES.getFrom(options);
        this.listOptions = ListOptions.getFrom(options);
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        return new HashSet<NodeRenderingHandler<? extends Node>>(Arrays.asList(
                new NodeRenderingHandler<AutoLink>(AutoLink.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<BlockQuote>(BlockQuote.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<BulletList>(BulletList.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<BulletListItem>(BulletListItem.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Code>(Code.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Document>(Document.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Emphasis>(Emphasis.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<FencedCodeBlock>(FencedCodeBlock.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<HardLineBreak>(HardLineBreak.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Heading>(Heading.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<HtmlBlock>(HtmlBlock.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<HtmlCommentBlock>(HtmlCommentBlock.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<HtmlEntity>(HtmlEntity.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<HtmlInline>(HtmlInline.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<HtmlInlineComment>(HtmlInlineComment.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<HtmlInnerBlock>(HtmlInnerBlock.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<HtmlInnerBlockComment>(HtmlInnerBlockComment.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Image>(Image.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<ImageRef>(ImageRef.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<IndentedCodeBlock>(IndentedCodeBlock.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Link>(Link.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<LinkRef>(LinkRef.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<MailLink>(MailLink.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<OrderedList>(OrderedList.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<OrderedListItem>(OrderedListItem.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Paragraph>(Paragraph.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Reference>(Reference.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<SoftLineBreak>(SoftLineBreak.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<StrongEmphasis>(StrongEmphasis.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<Text>(Text.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<TextBase>(TextBase.class, YouTrackConverterNodeRenderer.this::render),
                new NodeRenderingHandler<ThematicBreak>(ThematicBreak.class, YouTrackConverterNodeRenderer.this::render)
        ));
    }

    private void render(Document node, NodeRendererContext context, HtmlWriter html) {
        // No rendering itself
        context.renderChildren(node);
    }

    private String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private void render(Heading node, NodeRendererContext context, HtmlWriter html) {
        String s = repeat("=", node.getLevel());
        html.line().raw(s);
        context.renderChildren(node);
        html.raw(s);
        tailBlankLine(node, html);
    }

    private HtmlWriter tailBlankLine(Node node, HtmlWriter html) {
        return tailBlankLine(node, 1, html);
    }

    public boolean isLastBlockQuoteChild(Node node) {
        Node parent = node.getParent();
        return parent instanceof BlockQuote && parent.getLastChild() == node;
    }

    public HtmlWriter tailBlankLine(Node node, int count, HtmlWriter html) {
        if (isLastBlockQuoteChild(node)) {
            // Needed to not add block quote prefix to trailing blank lines
            //if (getPushedPrefixCount() > 0) {
            //    flush(-1); // clear pending lines so pop prefix is not delayed, if PREFIX_AFTER_PENDING_EOL is enabled
            //    popPrefix();
            //    pushPrefix();
            //}
            CharSequence prefix = html.getPrefix();
            html.popPrefix();
            html.blankLine(count);
            html.pushPrefix();
            html.setPrefix(prefix, false);
        } else {
            html.blankLine(count);
        }
        return html;
    }

    private void render(BlockQuote node, NodeRendererContext context, HtmlWriter html) {
        inBlockQuote++;
        String repeat = repeat(">", inBlockQuote) + " ";

        html.pushPrefix();
        html.line().setPrefix("").raw(repeat);
        html.setPrefix(repeat);
        context.renderChildren(node);

        inBlockQuote--;
        html.popPrefix();

        tailBlankLine(node, html);
    }

    private void render(FencedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        BasedSequence info = node.getInfo();
        if (info.isNotNull() && !info.isBlank()) {
            html.line().raw("{code:lang=" + info.unescape() + "}").line();
        } else {
            html.line().raw("{code}").line();
        }

        html.raw(node.getContentChars().normalizeEOL());
        html.line().raw("{code}");
        tailBlankLine(node, html);
    }

    private void render(ThematicBreak node, NodeRendererContext context, HtmlWriter html) {
        html.line().raw("-----");
        tailBlankLine(node, html);
    }

    private void render(IndentedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        html.line().raw("{noformat}").line();
        html.raw(node.getContentChars().trimTailBlankLines().normalizeEndWithEOL());
        html.line().raw("{noformat}").line();
    }

    private void renderListItemPrefix(ListItem node, NodeRendererContext context, HtmlWriter html) {
        Node parent = node;
        StringBuilder prefix = new StringBuilder();
        while (parent instanceof ListBlock || parent instanceof ListItem) {
            if (parent instanceof BulletList) {
                prefix.append('*');
            } else if (parent instanceof OrderedList) {
                prefix.append('#');
            }
            parent = parent.getParent();
        }

        if (prefix.length() > 0) {
            prefix.append(' ');
        }
        html.line().raw(prefix.toString());
    }

    private void renderListItem(ListItem node, NodeRendererContext context, HtmlWriter html) {
        renderListItemPrefix(node, context, html);
        if (listOptions.isTightListItem(node)) {
            context.renderChildren(node);
        } else {
            context.renderChildren(node);
            if (node.getFirstChild().getNext() != null) {
                tailBlankLine(node, html);
            }
        }
    }

    private void renderList(ListBlock node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
        if (node.getParent() instanceof Document) {
            if (node.getLastChild() == null || listOptions.isTightListItem((ListItem) node.getLastChild())) {
                tailBlankLine(node, html);
            }
        }
    }

    private void render(BulletList node, NodeRendererContext context, HtmlWriter html) {
        renderList(node, context, html);
    }

    private void render(OrderedList node, NodeRendererContext context, HtmlWriter html) {
        renderList(node, context, html);
    }

    private void render(BulletListItem node, NodeRendererContext context, HtmlWriter html) {
        renderListItem(node, context, html);
    }

    private void render(OrderedListItem node, NodeRendererContext context, HtmlWriter html) {
        renderListItem(node, context, html);
    }

    private static void renderTextBlockParagraphLines(Node node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
        html.line();
    }

    private void renderLooseParagraph(Paragraph node, NodeRendererContext context, HtmlWriter html) {
        renderTextBlockParagraphLines(node, context, html);

        if (inBlockQuote > 0 && node.getNext() == null) {
            html.line();
        } else {
            tailBlankLine(node, html);
        }
    }

    private void render(Paragraph node, NodeRendererContext context, HtmlWriter html) {
        if (!(node.getParent() instanceof ParagraphItemContainer)
                || !((ParagraphItemContainer) node.getParent()).isParagraphWrappingDisabled(node, listOptions, context.getOptions())) {
            renderLooseParagraph(node, context, html);
        } else {
            renderTextBlockParagraphLines(node, context, html);
        }
    }

    public static BasedSequence getSoftLineBreakSpan(Node node) {
        if (node == null) return NULL;

        Node lastNode = node;
        Node nextNode = node.getNext();

        while (nextNode != null && !(nextNode instanceof SoftLineBreak)) {
            lastNode = nextNode;
            nextNode = nextNode.getNext();
        }

        return Node.spanningChars(node.getChars(), lastNode.getChars());
    }

    private void render(SoftLineBreak node, NodeRendererContext context, HtmlWriter html) {
        html.raw(" ");
    }

    private void render(HardLineBreak node, NodeRendererContext context, HtmlWriter html) {
        //html.raw("\\\\");
        html.line();
    }

    private void render(Emphasis node, NodeRendererContext context, HtmlWriter html) {
        html.raw("''");
        context.renderChildren(node);
        html.raw("''");
    }

    private void render(StrongEmphasis node, NodeRendererContext context, HtmlWriter html) {
        html.raw("*");
        context.renderChildren(node);
        html.raw("*");
    }

    private void render(Text node, NodeRendererContext context, HtmlWriter html) {
        html.raw(Escaping.normalizeEOL(node.getChars().unescape()));
    }

    private void render(TextBase node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
    }

    private void render(Code node, NodeRendererContext context, HtmlWriter html) {
        html.raw("`");
        html.raw(Escaping.collapseWhitespace(node.getText(), true));
        html.raw("`");
    }

    private void render(HtmlBlock node, NodeRendererContext context, HtmlWriter html) {
        if (node.hasChildren()) {
            // inner blocks handle rendering
            context.renderChildren(node);
        } else {
            renderHtmlBlock(node, context, html, context.getHtmlOptions().suppressHtmlBlocks, context.getHtmlOptions().escapeHtmlBlocks);
        }
    }

    private void render(HtmlCommentBlock node, NodeRendererContext context, HtmlWriter html) {
        renderHtmlBlock(node, context, html, context.getHtmlOptions().suppressHtmlCommentBlocks, context.getHtmlOptions().escapeHtmlCommentBlocks);
    }

    private void render(HtmlInnerBlock node, NodeRendererContext context, HtmlWriter html) {
        renderHtmlBlock(node, context, html, context.getHtmlOptions().suppressHtmlBlocks, context.getHtmlOptions().escapeHtmlBlocks);
    }

    private void render(HtmlInnerBlockComment node, NodeRendererContext context, HtmlWriter html) {
        renderHtmlBlock(node, context, html, context.getHtmlOptions().suppressHtmlCommentBlocks, context.getHtmlOptions().escapeHtmlCommentBlocks);
    }

    public void renderHtmlBlock(HtmlBlockBase node, NodeRendererContext context, HtmlWriter html, boolean suppress, boolean escape) {
        if (suppress) return;

        html.line().raw("{code:html}").line();
        html.raw(node.getContentChars().normalizeEOL());
        html.line().raw("{code:html}").line();
    }

    private void render(HtmlInline node, NodeRendererContext context, HtmlWriter html) {
        renderInlineHtml(node, context, html, context.getHtmlOptions().suppressInlineHtml, context.getHtmlOptions().escapeInlineHtml);
    }

    private void render(HtmlInlineComment node, NodeRendererContext context, HtmlWriter html) {
        renderInlineHtml(node, context, html, context.getHtmlOptions().suppressInlineHtmlComments, context.getHtmlOptions().escapeInlineHtmlComments);
    }

    public void renderInlineHtml(HtmlInlineBase node, NodeRendererContext context, HtmlWriter html, boolean suppress, boolean escape) {
        if (suppress) return;
        html.raw("`").raw(node.getChars().normalizeEOL()).raw("`");
    }

    private void render(Reference node, NodeRendererContext context, HtmlWriter html) {

    }

    private void render(HtmlEntity node, NodeRendererContext context, HtmlWriter html) {
        html.raw(node.getChars().unescape());
    }

    private void render(AutoLink node, NodeRendererContext context, HtmlWriter html) {
        String text = node.getText().toString();
        if (context.isDoNotRenderLinks()) {
            html.text(text);
        } else {
            ResolvedLink resolvedLink = context.resolveLink(LinkType.LINK, text, null);
            html.raw("[").raw(text).raw("|").raw(resolvedLink.getUrl());
        }
    }

    private void render(MailLink node, NodeRendererContext context, HtmlWriter html) {
        String text = node.getText().unescape();
        if (context.isDoNotRenderLinks()) {
            html.text(text);
        } else {
            ResolvedLink resolvedLink = context.resolveLink(LinkType.LINK, text, null);
            html.raw("[").raw(text).raw("|mailto:").raw(resolvedLink.getUrl()).raw("]");
        }
    }

    private void render(Image node, NodeRendererContext context, HtmlWriter html) {
        if (!context.isDoNotRenderLinks()) {
            String altText = new TextCollectingVisitor().collectAndGetText(node);

            ResolvedLink resolvedLink = context.resolveLink(LinkType.IMAGE, node.getUrl().unescape(), null);
            html.raw("!").raw(resolvedLink.getUrl()).raw("!");
        }
    }

    private void render(Link node, NodeRendererContext context, HtmlWriter html) {
        if (context.isDoNotRenderLinks()) {
            context.renderChildren(node);
        } else {
            ResolvedLink resolvedLink = context.resolveLink(LinkType.LINK, node.getUrl().unescape(), null);
            html.raw("[");
            context.renderChildren(node);
            html.raw("|").raw(resolvedLink.getUrl()).raw("]");
        }
    }

    private void render(ImageRef node, NodeRendererContext context, HtmlWriter html) {
        if (!node.isDefined() && recheckUndefinedReferences) {
            if (node.getReferenceNode(referenceRepository) != null) {
                node.setDefined(true);
            }
        }

        if (!node.isDefined()) {
            // empty ref, we treat it as text
            assert !node.isDefined();
            html.text(node.getChars().unescape());
        } else {
            if (!context.isDoNotRenderLinks()) {
                Reference reference = node.getReferenceNode(referenceRepository);
                assert reference != null;
                String altText = new TextCollectingVisitor().collectAndGetText(node);

                ResolvedLink resolvedLink = context.resolveLink(LinkType.IMAGE, reference.getUrl().unescape(), null);
                html.raw("!").raw(resolvedLink.getUrl()).raw("!");
            }
        }
    }

    private void render(LinkRef node, NodeRendererContext context, HtmlWriter html) {
        if (!node.isDefined() && recheckUndefinedReferences) {
            if (node.getReferenceNode(referenceRepository) != null) {
                node.setDefined(true);
            }
        }

        if (!node.isDefined()) {
            // empty ref, we treat it as text
            assert !node.isDefined();
            html.raw("[");
            context.renderChildren(node);
            html.raw("]");

            if (!node.isReferenceTextCombined()) {
                html.raw("[");
                html.raw(node.getReference().unescape());
                html.raw("]");
            }
        } else {
            if (context.isDoNotRenderLinks()) {
                context.renderChildren(node);
            } else {
                Reference reference = node.getReferenceNode(referenceRepository);
                assert reference != null;

                ResolvedLink resolvedLink = context.resolveLink(LinkType.LINK, reference.getUrl().unescape(), null);

                html.raw("[");
                context.renderChildren(node);
                html.raw("|");
                html.raw(resolvedLink.getUrl());
                html.raw("]");
            }
        }
    }

    public static class Factory implements NodeRendererFactory {
        @Override
        public NodeRenderer apply(DataHolder options) {
            return new YouTrackConverterNodeRenderer(options);
        }
    }
}
