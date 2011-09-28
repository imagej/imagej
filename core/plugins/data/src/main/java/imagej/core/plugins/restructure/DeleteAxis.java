//
// DeleteAxis.java
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

package imagej.core.plugins.restructure;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * Deletes an axis from an input Dataset.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Delete Axis...") })
public class DeleteAxis extends DynamicPlugin {

	private static final String NAME_KEY = "Axis to delete";
	private static final String POSITION_KEY = "Index of hyperplane to keep";

	private Dataset dataset;
	private String axisToDelete;
	private long oneBasedHyperplanePos;

	private long hyperPlaneToKeep;

	public DeleteAxis() {
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final ImageDisplay display = imageDisplayService.getActiveImageDisplay();
		if (display == null) return;
		dataset = imageDisplayService.getActiveDataset(display);

		final DefaultModuleItem<String> name =
			new DefaultModuleItem<String>(this, NAME_KEY, String.class);
		final List<Axis> datasetAxes = Arrays.asList(dataset.getAxes());
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis candidateAxis : Axes.values()) {
			if (Axes.isXY(candidateAxis)) continue;
			if (datasetAxes.contains(candidateAxis)) choices.add(candidateAxis
				.getLabel());
		}
		name.setChoices(choices);
		addInput(name);

		final DefaultModuleItem<Long> pos =
			new DefaultModuleItem<Long>(this, POSITION_KEY, Long.class);
		pos.setMinimumValue(1L);
		// TODO - set max value to number of hyperplanes along desired axis
		addInput(pos);
	}

	/**
	 * Creates new ImgPlus data with one less axis. sets pixels of ImgPlus to user
	 * specified hyperplane within original ImgPlus data. Assigns the new ImgPlus
	 * to the input Dataset.
	 */
	@Override
	public void run() {
		final Map<String, Object> inputs = getInputs();
		axisToDelete = (String) inputs.get(NAME_KEY);
		oneBasedHyperplanePos = (Long) inputs.get(POSITION_KEY);
		hyperPlaneToKeep = oneBasedHyperplanePos - 1;
		final Axis axis = Axes.get(axisToDelete);
		if (inputBad(axis)) return;
		final Axis[] newAxes = getNewAxes(dataset, axis);
		final long[] newDimensions = getNewDimensions(dataset, axis);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, newAxes);
		final int compositeCount =
			compositeStatus(dataset.getCompositeChannelCount(), dstImgPlus);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus);
		// TODO - colorTables, metadata, etc.?
		dstImgPlus.setCompositeChannelCount(compositeCount);
		dataset.setImgPlus(dstImgPlus);
	}

	/**
	 * Detects if user specified data is invalid
	 */
	private boolean inputBad(final Axis axis) {
		// axis not determined by dialog
		if (axis == null) return true;

		// axis not already present in Dataset
		final int axisIndex = dataset.getAxisIndex(axis);
		if (axisIndex < 0) return true;

		// hyperplane index out of range
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);
		if ((hyperPlaneToKeep < 0) || (hyperPlaneToKeep >= axisSize)) return true;

		return false;
	}

	/**
	 * Creates an Axis[] that consists of all the axes from a Dataset minus a user
	 * specified axis
	 */
	private Axis[] getNewAxes(final Dataset ds, final Axis axis) {
		final Axis[] origAxes = ds.getAxes();
		final Axis[] newAxes = new Axis[origAxes.length - 1];
		int index = 0;
		for (final Axis a : origAxes)
			if (a != axis) newAxes[index++] = a;
		return newAxes;
	}

	/**
	 * Creates a long[] that consists of all the dimensions from a Dataset minus a
	 * user specified axis.
	 */
	private long[] getNewDimensions(final Dataset ds, final Axis axis) {
		final long[] origDims = ds.getDims();
		final Axis[] origAxes = ds.getAxes();
		final long[] newDims = new long[origAxes.length - 1];
		int index = 0;
		for (int i = 0; i < origAxes.length; i++) {
			final Axis a = origAxes[i];
			if (a != axis) newDims[index++] = origDims[i];
		}
		return newDims;
	}

	/**
	 * Fills the data in the shrunken ImgPlus with the contents of the user
	 * specified hyperplane in the original image
	 */
	private void fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus)
	{
		final long[] srcOrigin = new long[srcImgPlus.numDimensions()];
		final long[] dstOrigin = new long[dstImgPlus.numDimensions()];

		final long[] srcSpan = new long[srcOrigin.length];
		final long[] dstSpan = new long[dstOrigin.length];

		srcImgPlus.dimensions(srcSpan);
		dstImgPlus.dimensions(dstSpan);

		final Axis axis = Axes.get(axisToDelete);
		final int axisIndex = srcImgPlus.getAxisIndex(axis);
		srcOrigin[axisIndex] = this.hyperPlaneToKeep;
		srcSpan[axisIndex] = 1;

		RestructureUtils.copyHyperVolume(srcImgPlus, srcOrigin, srcSpan,
			dstImgPlus, dstOrigin, dstSpan);
	}

	private int
		compositeStatus(final int compositeCount, final ImgPlus<?> output)
	{
		if (output.getAxisIndex(Axes.CHANNEL) < 0) return 1;
		return compositeCount;

	}
}
