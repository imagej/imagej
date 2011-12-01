//
// Plugin.java
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

package imagej.ext.plugin;

import imagej.ext.display.ActiveDisplayPreprocessor;
import imagej.ext.display.Display;
import imagej.ext.display.DisplayPostprocessor;
import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.debug.DebugPostprocessor;
import imagej.ext.plugin.debug.DebugPreprocessor;
import imagej.ext.plugin.process.PostprocessorPlugin;
import imagej.ext.plugin.process.PreprocessorPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

/**
 * Annotation identifying a plugin, which gets loaded by ImageJ's dynamic
 * discovery mechanism.
 * 
 * @author Curtis Rueden
 * @see IPlugin
 * @see PluginService
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type = IPlugin.class)
public @interface Plugin {

	/**
	 * Priority for processors that must go first in the processor chain.
	 * Examples: {@link DebugPreprocessor}, {@link DebugPostprocessor}
	 */
	double FIRST_PRIORITY = Double.NEGATIVE_INFINITY;

	/**
	 * Priority for processors that strongly prefer to be early in the processor
	 * chain. Examples: {@link ActiveDisplayPreprocessor},
	 * {@link ServicePreprocessor}
	 */
	double VERY_HIGH_PRIORITY = -10000;

	/**
	 * Priority for processors that prefer to be earlier in the processor chain.
	 * Example: {@link InitPreprocessor}
	 */
	double HIGH_PRIORITY = -100;

	/** Default priority for processors. */
	double NORMAL_PRIORITY = 0;

	/** Priority for processors that prefer to be later in the processor chain. */
	double LOW_PRIORITY = 100;

	/**
	 * Priority for processors that strongly prefer to be late in the processor
	 * chain. Examples: {@link DisplayPostprocessor}, UI-specific subclasses of
	 * {@link AbstractInputHarvesterPlugin}.
	 */
	double VERY_LOW_PRIORITY = 10000;

	/** Priority for processors that must go at the end of the processor chain. */
	double LAST_PRIORITY = Double.POSITIVE_INFINITY;

	/**
	 * The type of plugin; e.g., {@link ImageJPlugin}, {@link PreprocessorPlugin},
	 * {@link PostprocessorPlugin} or {@link Display}.
	 */
	Class<?> type() default ImageJPlugin.class;

	/** The name of the plugin. */
	String name() default "";

	/** The human-readable label to use (e.g., in the menu structure). */
	String label() default "";

	/** A longer description of the plugin (e.g., for use a tool tip). */
	String description() default "";

	/**
	 * Abbreviated menu path defining where the plugin is shown in the menu
	 * structure. Uses greater than signs (>) as a separator; e.g.:
	 * "Image > Overlay > Properties..." defines a "Properties..." menu item
	 * within the "Overlay" submenu of the "Image" menu. Use either
	 * {@link #menuPath} or {@link #menu} but not both.
	 */
	String menuPath() default "";

	/**
	 * Full menu path defining where the plugin is shown in the menu structure.
	 * This construction allows menus to be fully specified including mnemonics,
	 * accelerators and icons. Use either {@link #menuPath} or {@link #menu} but
	 * not both.
	 */
	Menu[] menu() default {};

	/** Path to the plugin's icon (e.g., shown in the menu structure). */
	String iconPath() default "";

	/**
	 * The plugin index returns plugins sorted by priority. This is useful for
	 * {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s to control the
	 * order of their execution.
	 * <p>
	 * Any double value is allowed, but for convenience, there are some presets:
	 * </p>
	 * <ul>
	 * <li>{@link #FIRST_PRIORITY}</li>
	 * <li>{@link #VERY_HIGH_PRIORITY}</li>
	 * <li>{@link #HIGH_PRIORITY}</li>
	 * <li>{@link #NORMAL_PRIORITY}</li>
	 * <li>{@link #LOW_PRIORITY}</li>
	 * <li>{@link #VERY_LOW_PRIORITY}</li>
	 * <li>{@link #LAST_PRIORITY}</li>
	 * </ul>
	 */
	double priority() default NORMAL_PRIORITY;

	/**
	 * Whether the plugin can be selected in the user interface. A plugin's
	 * selection state (if any) is typically rendered in the menu structure using
	 * a checkbox or radio button menu item (see {@link #selectionGroup}).
	 */
	boolean selectable() default false;

	/**
	 * For selectable plugins, specifies a name defining a group of linked
	 * plugins, only one of which is selected at any given time. Typically this is
	 * rendered in the menu structure as a group of radio button menu items. If no
	 * group is given, the plugin is assumed to be a standalone toggle, and
	 * typically rendered as as checkbox menu item.
	 */
	String selectionGroup() default "";

	/** When false, grays out the plugin in the user interface. */
	boolean enabled() default true;

	/** When false, the user interface will not provide a cancel button. */
	boolean cancelable() default true;

	/**
	 * Defines a function that is called during preprocessing to assign the
	 * plugin's initial input values. This initializer is called before the
	 * individual @{@link Parameter#initializer()} (i.e.,
	 * {@link ModuleItem#getInitializer()}) methods.
	 * 
	 * @see InitPreprocessor
	 */
	String initializer() default "";

}
