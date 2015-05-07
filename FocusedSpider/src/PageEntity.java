import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Mike
 * Date: 12-10-7
 * Time: 上午9:11
 * To change this template use File | Settings | File Templates.
 */
public class PageEntity {
    /* 页面的路径 */
    private String path;
    /* 页面内容 */
    private String content;
    /* 外链（本页面链向其它页面）*/
    private List<String> outLinks = new ArrayList<String>();
    /* 内链（其它页面链向本页面）*/
    private List<String> inLinks = new ArrayList<String>();
    /* 本页面的PageRank值 */
    private double pr;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getOutLinks() {
        return outLinks;
    }

    public void setOutLinks(List<String> outLinks) {
        this.outLinks = outLinks;
    }

    public List<String> getInLinks() {
        return inLinks;
    }

    public void setInLinks(List<String> inLinks) {
        this.inLinks = inLinks;
    }

    public double getPr() {
        return pr;
    }

    public void setPr(double pr) {
        this.pr = pr;
    }
}
