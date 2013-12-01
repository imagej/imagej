/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.platform;

import imagej.ImageJPlugin;

import java.io.IOException;
import java.net.URL;

import org.scijava.Disposable;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SingletonPlugin;

/**
 * An interface for configuring a specific deployment platform, defined by
 * criteria such as operating system, machine architecture or Java version.
 * <p>
 * Platforms discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link Platform}.class. While it possible to create a platform merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractPlatform}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see PlatformService
 */
public interface Platform extends ImageJPlugin, SingletonPlugin, Disposable {

	/** Java Runtime Environment vendor to match. */
	String javaVendor();

	/** Minimum required Java Runtime Environment version. */
	String javaVersion();

	/** Operating system architecture to match. */
	String osArch();

	/** Operating system name to match. */
	String osName();

	/** Minimum required operating system version. */
	String osVersion();

	/** Determines whether the given platform is applicable to this runtime. */
	boolean isTarget();

	/** Activates and configures the platform. */
	void configure(PlatformService service);

	void open(URL url) throws IOException;

	/**
	 * Informs the platform of a UI's newly created application menu structure.
	 * The platform may choose to do something platform-specific with the menus.
	 * 
	 * @param menus The UI's newly created menu structure
	 * @return true iff the menus should not be added to the UI as normal because
	 *         the platform did something platform-specific with them instead.
	 */
	boolean registerAppMenus(Object menus);

}
