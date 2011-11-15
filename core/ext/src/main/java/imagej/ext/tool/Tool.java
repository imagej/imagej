//
// Tool.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ext.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

/**
 * Annotation identifying a tool, which gets loaded by ImageJ's dynamic
 * discovery mechanism.
 * 
 * @author Rick Lentz
 * @author Curtis Rueden
 * @see ITool
 * @see ToolService
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type = ITool.class)
public @interface Tool {

	/** The name of the tool. */
	String name() default "";

	/** The human-readable label to use (e.g., as a tool tip). */
	String label() default "";

	/** A longer description of the tool (e.g., in the status bar). */
	String description() default "";

	/** Path to the tool's icon (e.g., shown in the toolbar). */
	String iconPath() default "";

	/**
	 * For tools in the toolbar (i.e., not-always-active tools), the priority
	 * defines where the tool should appear in the user interface. The toolbar
	 * displays tools sorted by priority.
	 * <p>
	 * For always-active tools, the priority defines the order in which they
	 * receive events. An always-active tool can consume an event, preventing
	 * lower-priority always-active tools from receiving it, so the priority order
	 * is important.
	 * </p>
	 */
	double priority() default Double.POSITIVE_INFINITY;

	/** When false, grays out the tool in the user interface. */
	boolean enabled() default true;

	/** When true, tool has no button but rather is active all the time. */
	boolean alwaysActive() default false;

	/**
	 * When true, tool receives events when the main ImageJ application frame is
	 * active. When false, tool only receives events when a display window is
	 * active.
	 */
	boolean activeInAppFrame() default false;

}
