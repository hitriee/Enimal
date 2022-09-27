import React from "react";
import { Link } from "react-router-dom";


import picdummy from "@assets/images/coco.jpeg";

function ArticleCard(props) {
  const { data } = props;
  
  return (
    <Link to='/community/detail/' className="mx-5">
      <div className="card_pic">
        <img src={picdummy} alt="게시글이미지" />
      </div>
      <div className="card_txt notoBold fs-20 text-center">
        {data.title}
      </div>
    </Link>
  )
}

export default ArticleCard;