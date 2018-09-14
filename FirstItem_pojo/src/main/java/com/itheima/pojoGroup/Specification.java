package com.itheima.pojoGroup;

import com.itheima.pojo.TbSpecification;
import com.itheima.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

/**
 * @author JAVA
 * @create 2018-09-13 20:16
 */
public class Specification  implements Serializable{
     private List<TbSpecificationOption> specificationOptionList;
      private TbSpecification specification;

    public List<TbSpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }

    public TbSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }

    public Specification(List<TbSpecificationOption> specificationOptionList, TbSpecification specification) {
        this.specificationOptionList = specificationOptionList;
        this.specification = specification;
    }

    public Specification() {
    }
}
