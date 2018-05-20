package top.yunzhitan.service;

import com.google.common.collect.Lists;
import top.yunzhitan.rpc.ServiceImpl;

import java.util.Collections;

@ServiceImpl
public class ServiceTestImpl extends BaseService implements ServiceTest {
        private String strValue;

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }

        @SuppressWarnings("NumericOverflow")
        @Override
        public ResultClass sayHello(String... s) {
            ResultClass result = new ResultClass();
            result.lon = Long.MIN_VALUE;
            Integer i = getIntValue();
            result.num = (i == null ? 0 : i);
            result.list = Lists.newArrayList("H", "e", "l", "l", "o");
            for (int j = 0; j < 5000; j++) {
                result.list.add(String.valueOf(Integer.MAX_VALUE - j));
            }
            Collections.addAll(result.list, s);
            return result;
        }
}
