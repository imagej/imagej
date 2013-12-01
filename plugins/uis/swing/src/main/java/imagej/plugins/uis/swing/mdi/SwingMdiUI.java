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

package imagej.plugins.uis.swing.mdi;

import imagej.display.Display;
import imagej.plugins.uis.swing.AbstractSwingUI;
import imagej.plugins.uis.swing.SwingApplicationFrame;
import imagej.plugins.uis.swing.mdi.viewer.SwingMdiDisplayWindow;
import imagej.ui.Desktop;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UIService;
import imagej.ui.UserInterface;
import imagej.ui.common.awt.AWTDropTargetEventDispatcher;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JScrollPane;

import org.scijava.Priority;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Swing-based MDI user interface for ImageJ.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class, name = SwingMdiUI.NAME,
	priority = Priority.LOW_PRIORITY)
public class SwingMdiUI extends AbstractSwingUI {

	public static final String NAME = "swing-mdi";

	@Parameter
	private EventService eventService;

	@Parameter
	private UIService uiService;

	private JMDIDesktopPane desktopPane;

	private JScrollPane scrollPane;

	// -- UserInterface methods --

	@Override
	public Desktop getDesktop() {
		return desktopPane;
	}

	@Override
	public SwingMdiDisplayWindow createDisplayWindow(Display<?> display) {
		final SwingMdiDisplayWindow displayWindow = new SwingMdiDisplayWindow();

		// broadcast internal frame events
		displayWindow
			.addEventDispatcher(new InternalFrameEventDispatcher(display));

		// broadcast drag-and-drop events
		new AWTDropTargetEventDispatcher(display, eventService);

		return displayWindow;
	}

	@Override
	public SwingMdiDialogPrompt dialogPrompt(final String message,
		final String title, final MessageType msg, final OptionType option)
	{
		final UserInterface ui = uiService.getDefaultUI();
		return new SwingMdiDialogPrompt(ui, message, title, msg, option);
	}

	// -- Internal methods --

	@Override
	protected void setupAppFrame() {
		final SwingApplicationFrame appFrame = getApplicationFrame();
		desktopPane = new JMDIDesktopPane();
		// TODO desktopPane.setTransferHandler(new DropFileTransferHandler());
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(desktopPane);
		desktopPane.setBackground(new Color(200, 200, 255));
		appFrame.getContentPane().add(scrollPane);
		appFrame.setBounds(getWorkSpaceBounds());
	}

	// -- Helper methods --

	private Rectangle getWorkSpaceBounds() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getMaximumWindowBounds();
	}

}
