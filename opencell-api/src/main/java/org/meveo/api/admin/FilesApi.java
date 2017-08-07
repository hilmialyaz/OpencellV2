package org.meveo.api.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.ejb.Stateless;

import org.apache.commons.io.FilenameUtils;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.admin.FileDto;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;

/**
 * @author Edward P. Legaspi
 */
@Stateless
public class FilesApi extends BaseApi {

	private ParamBean paramBean = ParamBean.getInstance();

	private String providerRootDir;

	public String getProviderRootDir() {
		if (StringUtils.isBlank(providerRootDir)) {
			providerRootDir = paramBean.getProperty("providers.rootDir", "./opencelldata") + File.separator
					+ appProvider.getCode();
		}

		return providerRootDir;
	}

	public List<FileDto> listFiles(String dir) throws BusinessApiException {
		if (!StringUtils.isBlank(dir)) {
			dir = getProviderRootDir() + File.separator + dir;
		} else {
			dir = getProviderRootDir();
		}

		File folder = new File(dir);

		if (folder.isFile()) {
			throw new BusinessApiException("Path " + dir + " is a file.");
		}

		List<FileDto> result = new ArrayList<FileDto>();

		List<File> files = Arrays.asList(folder.listFiles());
		if (files != null) {
			for (File file : files) {
				result.add(new FileDto(file));
			}
		}

		return result;
	}

	public void createDir(String dir) throws BusinessApiException {
		File file = new File(getProviderRootDir() + File.separator + dir);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public void suppressFile(String filePath) throws BusinessApiException {
		File file = new File(getProviderRootDir() + File.separator + filePath);
		if (!file.exists()) {
			throw new BusinessApiException("File does not exists: " + file.getPath());
		}

		try {
			FileUtils.archiveFile(file);
		} catch (IOException e) {
			throw new BusinessApiException("Error suppressing file: " + file.getName() + ". " + e.getMessage());
		}
	}

	public void suppressDir(String dir) throws BusinessApiException {
		File file = new File(getProviderRootDir() + File.separator + dir);
		if (!file.exists()) {
			throw new BusinessApiException("File does not exists: " + file.getPath());
		}

		try {
			FileOutputStream fos = new FileOutputStream(new File(
					FilenameUtils.removeExtension(file.getParent() + File.separator + file.getName()) + ".zip"));
			ZipOutputStream zos = new ZipOutputStream(fos);
			FileUtils.addDirToArchive(getProviderRootDir(), file.getPath(), zos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			throw new BusinessApiException("Error suppressing file: " + file.getName() + ". " + e.getMessage());
		}
	}

}
