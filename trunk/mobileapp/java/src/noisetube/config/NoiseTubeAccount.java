package noisetube.config;

public class NoiseTubeAccount
{

	private static final String sep = "#3";

	public String APIKey = "";
	public String username = "";
	public String password = "";

	public boolean isAvailable()
	{
		return !APIKey.equals("");
	}

	public static NoiseTubeAccount load(String db)
	{
		NoiseTubeAccount account = new NoiseTubeAccount();

		int idx_sep = db.indexOf(sep);
		account.APIKey = db.substring(0, idx_sep);

		String rest = db.substring(idx_sep + sep.length(), db.length());
		idx_sep = rest.indexOf(sep);
		account.username = rest.substring(0, idx_sep);

		rest = rest.substring(idx_sep + sep.length(), rest.length());
		account.password = rest;
		return account;
	}

	public String toString()
	{
		return APIKey + "#3" + username + "#3" + password;
	}

	// public static void main(String[] args)
	// {
	// NoiseTubeAccount account=NoiseTubeAccount.load("#3#3");
	// System.out.println(account.APIKey);
	// }

}
