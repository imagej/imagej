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

package imagej.updater.ui;

import imagej.updater.core.Conflicts;
import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.Dependency;
import imagej.updater.core.FileObject;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.FilesCollection.Filter;
import imagej.updater.core.FilesUploader;
import imagej.updater.core.Installer;
import imagej.updater.core.UpdateSite;
import imagej.updater.util.Downloadable;
import imagej.updater.util.Downloader;
import imagej.updater.util.Progress;
import imagej.updater.util.StderrProgress;
import imagej.updater.util.UpdaterUserInterface;
import imagej.updater.util.Util;
import imagej.util.AppUtils;

import java.awt.Frame;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scijava.log.LogService;

/**
 * This is the command-line interface into the ImageJ Updater.
 * 
 * @author Johannes Schindelin
 */
public class CommandLine {

	protected static LogService log = Util.getLogService();
	protected FilesCollection files;
	protected Progress progress;
	private boolean checksummed = false;

	/**
	 * Determines whether die() should exit or throw a RuntimeException.
	 */
	private boolean standalone;

	@Deprecated
	public CommandLine() {
		this(AppUtils.getBaseDirectory(), 80);
	}

	public CommandLine(final File ijDir, final int columnCount)
	{
		this(ijDir, columnCount, null);
	}

	public CommandLine(final File ijDir, final int columnCount, final Progress progress)
	{
		this.progress = progress == null ? new StderrProgress(columnCount) : progress;
		files = new FilesCollection(log, ijDir);
	}

	private void ensureChecksummed() {
		if (checksummed) return;
		String warnings;
		try {
			warnings = files.downloadIndexAndChecksum(progress);
		} catch (Exception e) {
			throw die("Received exception: " + e.getMessage());
		}
		if (!warnings.equals("")) System.err.println(warnings);
		checksummed = true;
	}

	protected class FileFilter implements Filter {

		protected Set<String> fileNames;

		public FileFilter(final List<String> files) {
			if (files != null && files.size() > 0) {
				fileNames = new HashSet<String>();
				for (final String file : files)
					fileNames.add(FileObject.getFilename(file, true));
			}
		}

		@Override
		public boolean matches(final FileObject file) {
			if (!file.isUpdateablePlatform(files)) return false;
			if (fileNames != null && !fileNames.contains(file.getFilename(true))) return false;
			return file.getStatus() != Status.OBSOLETE_UNINSTALLED;
		}
	}

	public void listCurrent(final List<String> list) {
		ensureChecksummed();
		for (final FileObject file : files.filter(new FileFilter(list)))
			System.out.println(file.filename + "-" + file.getTimestamp());
	}

	public void list(final List<String> list, Filter filter) {
		ensureChecksummed();
		if (filter == null) filter = new FileFilter(list);
		else filter = files.and(new FileFilter(list), filter);
		files.sort();
		for (final FileObject file : files.filter(filter))
			System.out.println(file.filename + "\t(" + file.getStatus() + ")\t" +
				file.getTimestamp());
	}

	public void list(final List<String> list) {
		list(list, null);
	}

	public void listUptodate(final List<String> list) {
		list(list, files.is(Status.INSTALLED));
	}

	public void listNotUptodate(final List<String> list) {
		list(list, files.not(files.oneOf(new Status[] { Status.OBSOLETE,
			Status.INSTALLED, Status.LOCAL_ONLY })));
	}

	public void listUpdateable(final List<String> list) {
		list(list, files.is(Status.UPDATEABLE));
	}

	public void listModified(final List<String> list) {
		list(list, files.is(Status.MODIFIED));
	}

	public void listLocalOnly(final List<String> list) {
		list(list, files.is(Status.LOCAL_ONLY));
	}

	public void show(final List<String> list) {
		for (String filename : list) {
			show(filename);
		}
	}

	public void show(final String filename) {
		ensureChecksummed();
		final FileObject file = files.get(filename);
		if (file == null) {
			System.err.println("\nERROR: File not found: " + filename);
		} else {
			show(file);
		}
	}

	public void show(final FileObject file) {
		ensureChecksummed();
		System.out.println();
		System.out.println("File: " + file.getFilename(true));
		if (!file.getFilename(true).equals(file.localFilename)) {
			System.out.println("(Local filename: " + file.localFilename + ")");
		}
		System.out.println("Update site: " + file.updateSite);
		if (file.current == null) {
			System.out.println("Removed from update site");
		} else {
			System.out.println("URL: " + files.getURL(file));
			System.out.println("checksum: " + file.current.checksum + ", timestamp: " + file.current.timestamp);
		}
		if (file.localChecksum != null && (file.current == null || !file.localChecksum.equals(file.current.checksum))) {
			System.out.println("Local checksum: " + file.localChecksum
					+ " (" + (file.hasPreviousVersion(file.localChecksum) ? "" : "NOT a ") + "previous version)");
		}
	}

	class OneFile implements Downloadable {

		FileObject file;

		OneFile(final FileObject file) {
			this.file = file;
		}

		@Override
		public File getDestination() {
			return files.prefix(file.filename);
		}

		@Override
		public String getURL() {
			return files.getURL(file);
		}

		@Override
		public long getFilesize() {
			return file.filesize;
		}

		@Override
		public String toString() {
			return file.filename;
		}
	}

	public void download(final FileObject file) {
		ensureChecksummed();
		try {
			new Downloader(progress, files.util).start(new OneFile(file));
			if (file.executable && !files.util.platform.startsWith("win")) try {
				Runtime.getRuntime()
					.exec(
						new String[] { "chmod", "0755",
							files.prefix(file.filename).getPath() });
			}
			catch (final Exception e) {
				e.printStackTrace();
				throw die("Could not mark " + file.filename +
					" as executable");
			}
			System.err.println("Installed " + file.filename);
		}
		catch (final IOException e) {
			System.err.println("IO error downloading " + file.filename + ": " +
				e.getMessage());
		}
	}

	public void delete(final FileObject file) {
		if (new File(file.filename).delete()) System.err.println("Deleted " +
			file.filename);
		else System.err.println("Failed to delete " + file.filename);
	}

	protected void addDependencies(final FileObject file,
		final Set<FileObject> all)
	{
		ensureChecksummed();
		if (all.contains(file)) return;
		all.add(file);
		for (final Dependency dependency : file.getDependencies()) {
			final FileObject file2 = files.get(dependency.filename);
			if (file2 != null) addDependencies(file2, all);
		}
	}

	public void update(final List<String> list) {
		update(list, false);
	}

	public void update(final List<String> list, final boolean force) {
		update(list, force, false);
	}

	public void update(final List<String> list, final boolean force,
		final boolean pristine)
	{
		ensureChecksummed();
		try {
			for (final FileObject file : files.filter(new FileFilter(list))) {
				if (file.getStatus() == Status.LOCAL_ONLY) {
					if (pristine)
						file.setAction(files, Action.UNINSTALL);
				} else if (file.isObsolete()) {
					if (file.getStatus() == Status.OBSOLETE) {
						log.info("Removing " + file.filename);
						file.stageForUninstall(files);
					}
					else if (file.getStatus() == Status.OBSOLETE_MODIFIED) {
						if (force || pristine) {
							file.stageForUninstall(files);
							log.info("Removing " + file.filename);
						}
						else
							log.warn("Skipping obsolete, but modified " + file.filename);
					}
				} else if (file.getStatus() != Status.INSTALLED && !file.stageForUpdate(files, force))
					log.warn("Skipping " + file.filename);
			}
			Installer installer = new Installer(files, progress);
			installer.start();
			installer.moveUpdatedIntoPlace();
			files.write();
		}
		catch (final Exception e) {
			if (e.getMessage().indexOf("conflicts") >= 0) {
				log.error("Could not update due to conflicts:");
				for (Conflict conflict : new Conflicts(files).getConflicts(false))
					log.error(conflict.getFilename() + ": " + conflict.getConflict());
			}
			else
				log.error("Error updating", e);
		}
	}

	public void upload(final List<String> list) {
		if (list == null) throw die("Which files do you mean to upload?");
		boolean forceUpdateSite = false;
		String updateSite = null;
		if (list.size() > 1 && "--update-site".equals(list.get(0))) {
			list.remove(0);
			updateSite = list.remove(0);
			forceUpdateSite = true;
		}
		if (list.size() == 0) throw die("Which files do you mean to upload?");

		ensureChecksummed();
		int count = 0;
		for (final String name : list) {
			final FileObject file = files.get(name);
			if (file == null) throw die("No file '" + name + "' found!");
			if (file.getStatus() == Status.INSTALLED) {
				System.err.println("Skipping up-to-date " + name);
				continue;
			}
			handleLauncherForUpload(file);
			if (updateSite == null) {
				updateSite = file.updateSite;
				if (updateSite == null) updateSite =
					file.updateSite = chooseUploadSite(name);
				if (updateSite == null) throw die("Canceled");
			}
			else if (file.updateSite == null) {
				System.err.println("Uploading new file '" + name + "' to  site '" +
					updateSite + "'");
				file.updateSite = updateSite;
			}
			else if (!file.updateSite.equals(updateSite)) {
				if (forceUpdateSite) {
					file.updateSite = updateSite;
				} else {
					throw die("Cannot upload to multiple update sites ("
							+ list.get(0) + " to " + updateSite + " and "
							+ name + " to " + file.updateSite + ")");
				}
			}
			if (file.getStatus() == Status.NOT_INSTALLED || file.getStatus() == Status.NEW) {
				System.err.println("Removing file '" + name + "'");
				file.setAction(files, Action.REMOVE);
			} else {
				file.setAction(files, Action.UPLOAD);
			}
			count++;
		}
		if (count == 0) {
			System.err.println("Nothing to upload");
			return;
		}

		if (updateSite != null && files.getUpdateSite(updateSite) == null) {
			throw die("Unknown update site: '" + updateSite + "'");
		}

		System.err.println("Uploading to " + getLongUpdateSiteName(updateSite));
		upload(updateSite);
	}

	public void uploadCompleteSite(final List<String> list) {
		if (list == null) throw die("Which files do you mean to upload?");
		boolean ignoreWarnings = false, forceShadow = false, simulate = false;
		while (list.size() > 0 && list.get(0).startsWith("-")) {
			final String option = list.remove(0);
			if ("--force".equals(option)) {
				ignoreWarnings = true;
			} else if ("--force-shadow".equals(option)) {
				forceShadow = true;
			} else if ("--simulate".equals(option)) {
				simulate = true;
			} else {
				throw die("Unknown option: " + option);
			}
		}
		if (list.size() != 1) throw die("Which files do you mean to upload?");
		String updateSite = list.get(0);

		ensureChecksummed();
		if (files.getUpdateSite(updateSite) == null) {
			throw die("Unknown update site '" + updateSite + "'");
		}

		int removeCount = 0, uploadCount = 0, warningCount = 0;
		for (final FileObject file : files) {
			final String name = file.filename;
			handleLauncherForUpload(file);
			switch (file.getStatus()) {
			case OBSOLETE:
			case OBSOLETE_MODIFIED:
				if (forceShadow || (ignoreWarnings && updateSite.equals(file.updateSite))) {
					file.updateSite = updateSite;
					file.setAction(files, Action.UPLOAD);
					if (simulate) System.err.println("Would upload " + file.filename);
					uploadCount++;
				} else {
					System.err.println("Warning: obsolete '" + name + "' still installed!");
					warningCount++;
				}
				break;
			case UPDATEABLE:
			case MODIFIED:
				if (!forceShadow && !updateSite.equals(file.updateSite)) {
					System.err.println("Warning: '" + name + "' of update site '"
							+ file.updateSite + "' is not up-to-date!");
					warningCount++;
					continue;
				}
				//$FALL-THROUGH$
			case LOCAL_ONLY:
				file.updateSite = updateSite;
				file.setAction(files, Action.UPLOAD);
				if (simulate) System.err.println("Would upload new " + (file.getStatus() == Status.LOCAL_ONLY ? "" : "version of ") + file.getLocalFilename(true));
				uploadCount++;
				break;
			case NEW:
			case NOT_INSTALLED:
				// special: keep tools-1.4.2.jar, needed for ImageJ 1.x
				if ("ImageJ".equals(updateSite) && file.getFilename(true).equals("jars/tools.jar")) break;
				file.setAction(files, Action.REMOVE);
				if (simulate) System.err.println("Would mark " + file.filename + " obsolete");
				removeCount++;
				break;
			case INSTALLED:
			case OBSOLETE_UNINSTALLED:
				// leave these alone
				break;
			}
		}

		// remove all obsolete dependencies of the same upload site
		for (final FileObject file : files.forUpdateSite(updateSite)) {
			if (!file.willBeUpToDate()) continue;
			for (final FileObject dependency : file.getFileDependencies(files, false)) {
				if (dependency.willNotBeInstalled() && updateSite.equals(dependency.updateSite)) {
					file.removeDependency(dependency.getFilename(false));
				}
			}
		}

		if (!ignoreWarnings && warningCount > 0) {
			throw die("Use --force to ignore warnings and upload anyway");
		}

		if (removeCount == 0 && uploadCount == 0) {
			System.err.println("Nothing to upload");
			return;
		}

		if (simulate) {
			final Iterable<Conflict> conflicts = new Conflicts(files).getConflicts(true);
			if (Conflicts.needsFeedback(conflicts)) {
				System.err.println("Unresolved upload conflicts!\n\n"
					+ Util.join("\n", conflicts));
			} else {
				System.err.println("Would upload " + uploadCount + " (removing " + removeCount
					+ ") to " + getLongUpdateSiteName(updateSite));
			}
			return;
		}

		System.err.println("Uploading " + uploadCount + " (removing " + removeCount
				+ ") to " + getLongUpdateSiteName(updateSite));
		upload(updateSite);
	}

	private void handleLauncherForUpload(final FileObject file) {
		if (file.getStatus() == Status.LOCAL_ONLY && files.util.isLauncher(file.filename)) {
			file.executable = true;
			file.addPlatform(Util.platformForLauncher(file.filename));
			for (final String fileName : new String[] { "jars/ij-launcher.jar" }) {
				final FileObject dependency = files.get(fileName);
				if (dependency != null) file.addDependency(files, dependency);
			}
		}
	}

	private void upload(final String updateSite) {
		FilesUploader uploader = null;
		try {
			uploader = new FilesUploader(null, files, updateSite, progress);
			if (!uploader.login())
				throw die("Login failed!");
			uploader.upload(progress);
			files.write();
		}
		catch (final Throwable e) {
			final String message = e.getMessage();
			if (message != null && message.indexOf("conflicts") >= 0) {
				log.error("Could not upload due to conflicts:");
				for (Conflict conflict : new Conflicts(files).getConflicts(true))
					log.error(conflict.getFilename() + ": " + conflict.getConflict());
			}
			else {
				e.printStackTrace();
				throw die("Error during upload: " + e);
			}
			if (uploader != null)
				uploader.logout();
		}
	}

	public String chooseUploadSite(final String file) {
		final List<String> names = new ArrayList<String>();
		final List<String> options = new ArrayList<String>();
		for (final String name : files.getUpdateSiteNames()) {
			final UpdateSite updateSite = files.getUpdateSite(name);
			if (updateSite.getUploadDirectory() == null ||
				updateSite.getUploadDirectory().equals("")) continue;
			names.add(name);
			options.add(getLongUpdateSiteName(name));
		}
		if (names.size() == 0) {
			System.err.println("No uploadable sites found");
			return null;
		}
		final String message = "Choose upload site for file '" + file + "'";
		final int index =
			UpdaterUserInterface.get().optionDialog(message, message,
				options.toArray(new String[options.size()]), 0);
		return index < 0 ? null : names.get(index);
	}

	public String getLongUpdateSiteName(final String name) {
		final UpdateSite site = files.getUpdateSite(name);
		return name + " (" +
			(site.getHost() == null || site.equals("") ? "" : site.getHost() + ":") +
			site.getUploadDirectory() + ")";
	}

	public void listUpdateSites(Collection<String> args) {
		ensureChecksummed();
		if (args == null || args.size() == 0)
			args = files.getUpdateSiteNames();
		for (final String name : args) {
			final UpdateSite site = files.getUpdateSite(name);
			System.out.print(name + ": " + site.getURL());
			if (site.getUploadDirectory() == null)
				System.out.println();
			else
				System.out.println(" (upload host: " + site.getHost() + ", upload directory: " + site.getUploadDirectory());
		}
	}

	public void addOrEditUploadSite(final List<String> args, boolean add) {
		if (args.size() != 2 && args.size() != 4)
			throw die("Usage: " + (add ? "add" : "edit") + "-update-site <name> <url> [<host> <upload-directory>]");
		addOrEditUploadSite(args.get(0), args.get(1), args.size() > 2 ? args.get(2) : null, args.size() > 3 ? args.get(3) : null, add);
	}

	public void addOrEditUploadSite(final String name, final String url, final String sshHost, final String uploadDirectory, boolean add) {
		ensureChecksummed();
		UpdateSite site = files.getUpdateSite(name);
		if (add) {
			if (site != null)
				throw die("Site '" + name + "' was already added!");
			files.addUpdateSite(name, url, sshHost, uploadDirectory, 0l);
		}
		else {
			if (site == null)
				throw die("Site '" + name + "' was not yet added!");
			site.setURL(url);
			site.setHost(sshHost);
			site.setUploadDirectory(uploadDirectory);
		}
		try {
			files.write();
		} catch (Exception e) {
			UpdaterUserInterface.get().handleException(e);
			throw die("Could not write local file database");
		}
	}

	public void removeUploadSite(final List<String> names) {
		if (names == null || names.size() < 1) {
			throw die("Which update-site do you want to remove, exactly?");
		}
		removeUploadSite(names.toArray(new String[names.size()]));
	}

	public void removeUploadSite(final String... names) {
		ensureChecksummed();
		for (final String name : names) {
			files.removeUpdateSite(name);
		}
		try {
			files.write();
		} catch (Exception e) {
			UpdaterUserInterface.get().handleException(e);
			throw die("Could not write local file database");
		}
	}

	@Deprecated
	public static CommandLine getInstance() {
		try {
			return new CommandLine();
		}
		catch (final Exception e) {
			e.printStackTrace();
			System.err.println("Could not parse db.xml.gz: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private static List<String> makeList(final String[] list, int start) {
		final List<String> result = new ArrayList<String>();
		while (start < list.length)
			result.add(list[start++]);
		return result;
	}

	/**
	 * Print an error message and exit the process with an error.
	 * 
	 * Note: Java has no "noreturn" annotation, but you can always write:
	 * <code>throw die(<message>)</code> to make the Java compiler understand.
	 * 
	 * @param message
	 *            the error message
	 * @return a dummy return value to be able to use "throw die(...)" to shut
	 *         up the compiler
	 */
	private RuntimeException die(final String message) {
		if (standalone) {
			System.err.println(message);
			System.exit(1);
		}
		return new RuntimeException(message);
	}

	public void usage() {
		throw die("Usage: imagej.updater.ui.CommandLine <command>\n"
			+ "\n" + "Commands:\n" + "\tlist [<files>]\n"
			+ "\tlist-uptodate [<files>]\n"
			+ "\tlist-not-uptodate [<files>]\n"
			+ "\tlist-updateable [<files>]\n"
			+ "\tlist-modified [<files>]\n"
			+ "\tlist-current [<files>]\n"
			+ "\tlist-local-only [<files>]\n"
			+ "\tshow [<files>]\n"
			+ "\tupdate [<files>]\n"
			+ "\tupdate-force [<files>]\n"
			+ "\tupdate-force-pristine [<files>]\n"
			+ "\tupload [--update-site <name>] [<files>]\n"
			+ "\tupload-complete-site [--simulate] [--force] [--force-shadow] <name>\n"
			+ "\tlist-update-sites [<nick>...]\n"
			+ "\tadd-update-site <nick> <url> [<host> <upload-directory>]\n"
			+ "\tedit-update-site <nick> <url> [<host> <upload-directory>]");
	}

	public static void main(final String... args) {
		try {
			main(AppUtils.getBaseDirectory(), 80, null, true, args);
		}
		catch (final RuntimeException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		catch (final Exception e) {
			e.printStackTrace();
			System.err.println("Could not parse db.xml.gz: " + e.getMessage());
			System.exit(1);
		}
	}

	public static void main(final File ijDir, final int columnCount, final String... args) {
		main(ijDir, columnCount, null, args);
	}

	public static void main(final File ijDir, final int columnCount, final Progress progress, final String... args) {
		main(ijDir, columnCount, progress, false, args);
	}

	private static void main(final File ijDir, final int columnCount,
			final Progress progress, final boolean standalone,
			final String[] args) {
		String http_proxy = System.getenv("http_proxy");
		if (http_proxy != null && http_proxy.startsWith("http://")) {
			final int colon = http_proxy.indexOf(':', 7);
			final int slash = http_proxy.indexOf('/', 7);
			int port = 80;
			if (colon < 0) http_proxy =
				slash < 0 ? http_proxy.substring(7) : http_proxy.substring(7, slash);
			else {
				port =
					Integer.parseInt(slash < 0 ? http_proxy.substring(colon + 1)
						: http_proxy.substring(colon + 1, slash));
				http_proxy = http_proxy.substring(7, colon);
			}
			System.setProperty("http.proxyHost", http_proxy);
			System.setProperty("http.proxyPort", "" + port);
		}
		else Util.useSystemProxies();
		Authenticator.setDefault(new ProxyAuthenticator());
		setUserInterface();

		final CommandLine instance = new CommandLine(ijDir, columnCount, progress);
		instance.standalone = standalone;

		if (args.length == 0) {
			instance.usage();
		}

		final String command = args[0];
		if (command.equals("list")) instance.list(makeList(args, 1));
		else if (command.equals("list-current")) instance.listCurrent(
			makeList(args, 1));
		else if (command.equals("list-uptodate")) instance.listUptodate(
			makeList(args, 1));
		else if (command.equals("list-not-uptodate")) instance
			.listNotUptodate(makeList(args, 1));
		else if (command.equals("list-updateable")) instance.listUpdateable(
			makeList(args, 1));
		else if (command.equals("list-modified")) instance.listModified(
			makeList(args, 1));
		else if (command.equals("list-local-only")) instance.listLocalOnly(
				makeList(args, 1));
		else if (command.equals("show")) instance.show(makeList(args, 1));
		else if (command.equals("update")) instance.update(makeList(args, 1));
		else if (command.equals("update-force")) instance.update(
			makeList(args, 1), true);
		else if (command.equals("update-force-pristine")) instance.update(
			makeList(args, 1), true, true);
		else if (command.equals("upload")) instance.upload(makeList(args, 1));
		else if (command.equals("upload-complete-site"))
			instance.uploadCompleteSite(makeList(args, 1));
		else if (command.equals("list-update-sites"))
			instance.listUpdateSites(makeList(args, 1));
		else if (command.equals("add-update-site"))
			instance.addOrEditUploadSite(makeList(args, 1), true);
		else if (command.equals("edit-update-site"))
			instance.addOrEditUploadSite(makeList(args, 1), false);
		else if (command.equals("remove-update-site"))
			instance.removeUploadSite(makeList(args, 1));
		else instance.usage();
	}

	protected static class ProxyAuthenticator extends Authenticator {

		protected Console console = System.console();

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			final String user =
				console.readLine("                                  \rProxy User: ");
			final char[] password = console.readPassword("Proxy Password: ");
			return new PasswordAuthentication(user, password);
		}
	}

	protected static void setUserInterface() {
		UpdaterUserInterface.set(new ConsoleUserInterface());
	}

	protected static class ConsoleUserInterface extends UpdaterUserInterface {

		protected Console console = System.console();
		protected int count;

		@Override
		public String getPassword(final String message) {
			if (console == null) {
				throw new RuntimeException("Password prompt requires interactive operation!");
			}
			System.out.print(message + ": ");
			return new String(console.readPassword());
		}

		@Override
		public boolean promptYesNo(final String title, final String message) {
			System.err.print(title + ": " + message);
			final String line = console.readLine();
			return line.startsWith("y") || line.startsWith("Y");
		}

		public void showPrompt(String prompt) {
			if (!prompt.endsWith(": ")) prompt += ": ";
			System.err.print(prompt);
		}

		public String getUsername(final String prompt) {
			showPrompt(prompt);
			return console.readLine();
		}

		public int askChoice(final String[] options) {
			for (int i = 0; i < options.length; i++)
				System.err.println("" + (i + 1) + ": " + options[i]);
			for (;;) {
				System.err.print("Choice? ");
				final String answer = console.readLine();
				if (answer.equals("")) return -1;
				try {
					return Integer.parseInt(answer) - 1;
				}
				catch (final Exception e) { /* ignore */}
			}
		}

		@Override
		public void error(final String message) {
			log.error(message);
		}

		@Override
		public void info(final String message, final String title) {
			log.info(title + ": " + message);
		}

		@Override
		public void log(final String message) {
			log.info(message);
		}

		@Override
		public void debug(final String message) {
			log.debug(message);
		}

		@Override
		public OutputStream getOutputStream() {
			return System.out;
		}

		@Override
		public void showStatus(final String message) {
			log(message);
		}

		@Override
		public void handleException(final Throwable exception) {
			exception.printStackTrace();
		}

		@Override
		public boolean isBatchMode() {
			return false;
		}

		@Override
		public int optionDialog(final String message, final String title,
			final Object[] options, final int def)
		{
			for (int i = 0; i < options.length; i++)
				System.err.println("" + (i + 1) + ") " + options[i]);
			for (;;) {
				System.out.print("Your choice (default: " + def + ": " + options[def] +
					")? ");
				final String line = console.readLine();
				if (line.equals("")) return def;
				try {
					final int option = Integer.parseInt(line);
					if (option > 0 && option <= options.length) return option - 1;
				}
				catch (final NumberFormatException e) {
					// ignore
				}
			}
		}

		@Override
		public String getPref(final String key) {
			return null;
		}

		@Override
		public void setPref(final String key, final String value) {
			log("Ignoring setting '" + key + "'");
		}

		@Override
		public void savePreferences() { /* do nothing */}

		@Override
		public void openURL(final String url) throws IOException {
			log("Please open " + url + " in your browser");
		}

		@Override
		public String getString(final String title) {
			System.out.print(title + ": ");
			return console.readLine();
		}

		@Override
		public void addWindow(final Frame window) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeWindow(final Frame window) {
			throw new UnsupportedOperationException();
		}
	}
}
