package softtest.registery.file;

import java.io.Serializable;

public class RegIdentity implements Serializable
{
	private final String id;

	private int num;

	private final int sequence;

	public static final int NEVER = 0;

	public static final int INITIAL = 1;

	public RegIdentity(String id, int num, int seq)
	{
		this.id = id.toUpperCase().trim();
		this.num = num;
		this.sequence = seq;
	}

	public String getID()
	{
		return this.id;
	}

	public int getNum()
	{
		return this.num;
	}

	public int getSeq()
	{
		return this.sequence;
	}

	public void updateNum()
	{
		--num;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true; // ???

		if (obj instanceof RegIdentity)
		{
			RegIdentity another = (RegIdentity) obj;
			if (this.num == another.num && this.sequence == another.sequence
					&& this.id.equalsIgnoreCase(another.id))
				return true;
		}
		return false;
	}
}
