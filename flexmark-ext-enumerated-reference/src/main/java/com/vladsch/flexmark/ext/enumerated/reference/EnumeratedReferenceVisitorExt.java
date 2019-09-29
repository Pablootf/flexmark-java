package com.vladsch.flexmark.ext.enumerated.reference;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class EnumeratedReferenceVisitorExt {
    public static <V extends EnumeratedReferenceVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<EnumeratedReferenceText>(EnumeratedReferenceText.class, visitor::visit),
                new VisitHandler<EnumeratedReferenceLink>(EnumeratedReferenceLink.class, visitor::visit),
                new VisitHandler<EnumeratedReferenceBlock>(EnumeratedReferenceBlock.class, visitor::visit),
        };
    }
}
