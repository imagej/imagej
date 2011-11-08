//
// LegacyOutputTracker.java
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

package imagej.legacy;

import ij.ImagePlus;

import java.util.HashSet;
import java.util.Set;

/**
 * The legacy output tracker is responsible for tracking important changes to
 * the IJ1 environment as a result of running a plugin. Important changes
 * include newly created {@link ImagePlus}es and {@link ImagePlus}es whose
 * window has closed.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyOutputTracker {

	// -- instance variables --

	/** Used to provide one list of {@link ImagePlus} per calling thread. */
	private static ThreadLocal<Set<ImagePlus>> outputImps =
		new ThreadLocal<Set<ImagePlus>>() {

			@Override
			protected synchronized Set<ImagePlus> initialValue() {
				return new HashSet<ImagePlus>();
			}
		};

	/** Used to provide one list of {@link ImagePlus} per calling thread. */
	private static ThreadLocal<Set<ImagePlus>> closedImps =
		new ThreadLocal<Set<ImagePlus>>() {

			@Override
			protected synchronized Set<ImagePlus> initialValue() {
				return new HashSet<ImagePlus>();
			}
		};

	/** Tracks which ImagePluses have their close() method initiated by IJ2 */
	private static ThreadLocal<Set<ImagePlus>> beingClosedByIJ2 =
		new ThreadLocal<Set<ImagePlus>>() {

			@Override
			protected synchronized Set<ImagePlus> initialValue() {
				return new HashSet<ImagePlus>();
			}
		};

	// -- public interface --

	/**
	 * Gets a list for storing the ImagePluses generated by a plugin. This method
	 * is (??) thread-safe, because it uses a separate set per thread.
	 */
	public static Set<ImagePlus> getOutputImps() {
		return outputImps.get();
	}

	/**
	 * Gets a list for storing the ImagePluses closed by a plugin. This method is
	 * (??) thread-safe, because it uses a separate set per thread.
	 */
	public static Set<ImagePlus> getClosedImps() {
		return closedImps.get();
	}

	/** Informs tracker that IJ2 has initiated the close() of an ImagePlus */
	public static void closeInitiatedByIJ2(final ImagePlus imp) {
		beingClosedByIJ2.get().add(imp);
	}

	/** Informs tracker that IJ2 has finished the close() of an ImagePlus */
	public static void closeCompletedByIJ2(final ImagePlus imp) {
		beingClosedByIJ2.get().remove(imp);
	}

	public static boolean isBeingClosedbyIJ2(final ImagePlus imp) {
		return beingClosedByIJ2.get().contains(imp);
	}
}
