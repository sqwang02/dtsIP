package softtest.registery.file;

import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter
{
	private String description = "";

	private ArrayList<String> extensions = new ArrayList<String>();

	public void addExtension(String extension)
	{
		if (!extension.startsWith("."))
		{
			extension = "." + extension;
		}

		extensions.add(extension.toLowerCase());
	}

	public boolean accept(File f)
	{
		boolean result = false;

		if (f.isDirectory())
			result = true;

		String name = f.getName().toLowerCase();
		for (String extension : extensions)
		{
			if (name.endsWith(extension))
				result = true;
		}

		return result;
	}

	public void setDescription(String aDescription)
	{
		this.description = aDescription;
	}

	public String getDescription()
	{
		return this.description;
	}

}
