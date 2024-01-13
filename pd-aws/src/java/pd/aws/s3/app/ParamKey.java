package pd.aws.s3.app;

public enum ParamKey {

    config,

    access_id,
    access_secret,
    region_name,
    endpoint_url,
    bucket_name,

    command,

    remote_prefix,
    prefix,
    remote_key,
    key;

    public String toOptString() {
        return "--" + name() + ":";
    }
}
