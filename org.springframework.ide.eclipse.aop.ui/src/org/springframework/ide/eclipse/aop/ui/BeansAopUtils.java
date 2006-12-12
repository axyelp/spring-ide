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

package org.springframework.ide.eclipse.aop.ui;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.Introspector.Statics;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Some helper methods.
 * 
 * @author Torsten Juergeleit
 */
public class BeansAopUtils {

    public static String getJavaElementLinkName(IJavaElement je) {
        // use element name instead, qualified with parent
        if (je instanceof IMethod) {
            return je.getParent().getElementName() + '.'
                    + readableName((IMethod) je);
        }
        else if (je.getParent() != null) {
            return je.getParent().getElementName() + '.' + je.getElementName();
        }
        return je.getElementName();
    }

    /**
     */
    private static String readableName(IMethod method) {

        StringBuffer buffer = new StringBuffer(method.getElementName());
        buffer.append('(');
        String[] parameterTypes = method.getParameterTypes();
        int length;
        if (parameterTypes != null && (length = parameterTypes.length) > 0) {
            for (int i = 0; i < length; i++) {
                buffer.append(Signature.toString(parameterTypes[i]));
                if (i < length - 1) {
                    buffer.append(", "); //$NON-NLS-1$
                }
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    public static String getElementDescription(IAopReference reference) {
        StringBuffer buf = new StringBuffer("<");
        buf.append(reference.getDefinition().getAspectName());
        buf.append("> [");
        buf.append(reference.getResource().getProjectRelativePath().toString());
        buf.append("]");
        return buf.toString();
    }
    
    public static void createProblemMarker(IResource resource, String message,
            int severity, int line, String markerId) {
        createProblemMarker(resource, message, severity, line, markerId, 1);
    }

    public static void createProblemMarker(IResource resource, String message,
            int severity, int line, String markerId, int markerCount) {
        if (resource != null && resource.isAccessible()) {
            try {

                // First check if specified marker already exists
                IMarker[] markers = resource.findMarkers(
                        BeansAopMarkerUtils.PROBLEM_MARKER, true,
                        IResource.DEPTH_ZERO);
                for (IMarker marker : markers) {
                    int l = marker.getAttribute(IMarker.LINE_NUMBER, -1);
                    int count = marker.getAttribute("marker_count", 1);
                    count++;
                    if (l == line) {
                        resource.findMarker(marker.getId()).delete();
                        createProblemMarker(resource, count + " AOP marker at this line", 1,
                                line, BeansAopMarkerUtils.PROBLEM_MARKER, count);
                        return;
                    }
                }

                // Create new marker
                IMarker marker = resource.createMarker(markerId);
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put(IMarker.MESSAGE, message);
                attributes.put(IMarker.SEVERITY, new Integer(severity));
                attributes.put("marker_count", markerCount);
                if (line > 0) {
                    attributes.put(IMarker.LINE_NUMBER, new Integer(line));
                }
                marker.setAttributes(attributes);
            }
            catch (CoreException e) {
                SpringCore.log(e);
            }
        }
    }

    public static URLClassLoader getProjectClassLoader(IJavaProject project) {
        List<URL> paths = getProjectClassPathURLs(project);
        URL pathUrls[] = (URL[]) paths.toArray(new URL[0]);
        return new URLClassLoader(pathUrls, Thread.currentThread()
                .getContextClassLoader());
    }

    public static List<URL> getProjectClassPathURLs(IJavaProject project) {
        List<URL> paths = new ArrayList<URL>();
        try {
            // configured classpath
            IClasspathEntry classpath[] = project.getRawClasspath();
            for (int i = 0; i < classpath.length; i++) {
                IClasspathEntry path = classpath[i];
                if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                    URL url = path.getPath().toFile().toURL();
                    paths.add(url);
                }
            }
            // build output, relative to project
            IPath location = getProjectLocation(project.getProject());
            IPath outputPath = location.append(project.getOutputLocation()
                    .removeFirstSegments(1));
            paths.add(outputPath.toFile().toURL());
        }
        catch (Exception e) {
        }
        return paths;
    }

    public static IPath getProjectLocation(IProject project) {
        if (project.getRawLocation() == null) {
            return project.getLocation();
        }
        else {
            return project.getRawLocation();
        }
    }

    public static IMethod getMethod(IType type, String methodName, int argCount) {
        try {
            return Introspector.findMethod(type, methodName, argCount, true,
                    Statics.DONT_CARE);
        }
        catch (JavaModelException e) {
        }
        return null;
    }

    public static Set<IFile> getFilesToBuild(IFile file) {
        Set<IFile> resourcesToBuild = new HashSet<IFile>();
        if (file.getName().endsWith(".java")) {
            IBeansProject project = BeansCorePlugin.getModel().getProject(
                    file.getProject());
            Set<IBeansConfig> configs = project.getConfigs();
            IJavaElement element = JavaCore.create(file);
            if (element instanceof ICompilationUnit) {
                try {
                    IType[] types = ((ICompilationUnit) element).getAllTypes();
                    List<String> typeNames = new ArrayList<String>();
                    for (IType type : types) {
                        typeNames.add(type.getFullyQualifiedName());
                    }
                    for (IBeansConfig config : configs) {
                        Set<String> allBeanClasses = config.getBeanClasses();
                        for (String className : allBeanClasses) {
                            if (typeNames.contains(className)) {
                                resourcesToBuild.add((IFile) config
                                        .getElementResource());
                            }
                        }
                    }
                }
                catch (JavaModelException e) {
                }
            }
        }
        return resourcesToBuild;
    }

    public static IJavaProject getJavaProject(IBeansConfig config) {
        IJavaProject project = JavaCore.create(config.getElementResource()
                .getProject());
        return project;
    }

    public static IJavaProject getJavaProject(IResource config) {
        IJavaProject project = JavaCore.create(config.getProject());
        return project;
    }

    public static int getLineNumber(IJavaElement element) {

        if (element != null && element instanceof IMethod) {
            try {
                IMethod method = (IMethod) element;
                int lines = 0;
                String targetsource;
                targetsource = method.getDeclaringType().getCompilationUnit()
                        .getSource();
                String sourceuptomethod = targetsource.substring(0, method
                        .getNameRange().getOffset());

                char[] chars = new char[sourceuptomethod.length()];
                sourceuptomethod.getChars(0, sourceuptomethod.length(), chars,
                        0);
                for (int j = 0; j < chars.length; j++) {
                    if (chars[j] == '\n') {
                        lines++;
                    }
                }
                return new Integer(lines + 1);
            }
            catch (JavaModelException e) {
            }
        }
        return new Integer(-1);
    }

}
