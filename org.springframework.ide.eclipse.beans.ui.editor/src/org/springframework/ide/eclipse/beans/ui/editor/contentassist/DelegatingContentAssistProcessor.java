/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;

@SuppressWarnings("restriction")
public class DelegatingContentAssistProcessor extends XMLContentAssistProcessor {

	protected void addAttributeValueProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addAttributeValueProposals(this, request);
		}
		super.addAttributeValueProposals(request);
	}

	protected void addAttributeNameProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addAttributeNameProposals(this, request);
		}
		super.addAttributeNameProposals(request);
	}

	protected void addTagCloseProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addTagCloseProposals(this, request);
		}
		super.addTagCloseProposals(request);
	}

	protected void addTagInsertionProposals(ContentAssistRequest request, int childPosition) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addTagInsertionProposals(this, request, childPosition);
		}
		super.addTagInsertionProposals(request, childPosition);
	}

	public ITextViewer getTextViewer() {
		return fTextViewer;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.', '=', '\"', '<' };
	}
}