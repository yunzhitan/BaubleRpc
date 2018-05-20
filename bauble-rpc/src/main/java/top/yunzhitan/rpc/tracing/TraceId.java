package top.yunzhitan.rpc.tracing;

import java.io.Serializable;


public class TraceId implements Serializable {

    private static final long serialVersionUID = 2901824755629719770L;

    public static final TraceId NULL_TRACE_ID = newInstance("null");

    private final String id;    // 全局唯一的ID
    private int node;           // 每经过一个节点, node的值会 +1

    public static TraceId newInstance(String id) {
        return new TraceId(id);
    }

    private TraceId(String id) {
        this.id = id;
        node = 0;
    }

    public String getId() {
        return id;
    }

    public int getNode() {
        return node;
    }

    public String asText() {
        return id + "_" + node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TraceId traceId = (TraceId) o;

        return node == traceId.node && id.equals(traceId.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + node;
        return result;
    }

    @Override
    public String toString() {
        return "TraceId{" +
                "id='" + id + '\'' +
                ", node=" + node +
                '}';
    }
}
