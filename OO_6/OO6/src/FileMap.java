class FileMap {
    private ObjFile key;
    private ObjFile value;
    FileMap(ObjFile key,ObjFile value) {
        this.key = key;
        this.value = value;
    }
    public ObjFile getKey() {
        return key;
    }
    public ObjFile getValue() {
        return value;
    }
}
