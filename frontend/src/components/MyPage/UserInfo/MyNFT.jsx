import React, { useState, useEffect } from "react";
import './MyNFT.scss'

import { getMyNFT } from "@apis/mypage";


import NFTtest from '@images/NFTtest.jpg'

import MakeNFTModal from "./MakeNFTModal";

function MyNFT(props) {
  const { userId } = props;
  const [myNFT, setMyNFT] = useState([]);
  const [openModal, setOpenModal] = useState(false);
  const [Animal, setAnimal] = useState(null);

  console.log(userId)

  useEffect(() => {
    getMyNFT(userId).then(res => {
      console.log(res)
      setMyNFT(res.data)
    })
  }, [])


  const closeModal = () => {
    setOpenModal(false);
  };


  function makeNFT(e) {
    setAnimal(e.target.id)
    setOpenModal(true)
  }
  return (
    <div>
      <h1 className="fs-40 notoBold myCollection">보유중인 컬렉션</h1>
      {myNFT.length > 0 ?
        <ul className="NFTList">
          {myNFT.map(nft => {
            console.log(nft)
            return (
              <div className="flex mx-5">
                {nft.info === false ?
                  <div className="NoNFT">
                    <li className="NFTcard" style={{ opacity: "30%" }}>
                      <img className="NFTImg" src={NFTtest} alt="#" />
                      <h1 className="fs-32 notoBold my-3">{nft.animal}</h1>
                    </li>
                    {localStorage.MyNick === userId ?
                      <button type="button" onClick={e => makeNFT(e)} id={nft.animal} className="fs-32 notoBold MakeNFT">NFT 제작하기</button> : null}
                  </div>

                  :
                  <li className="NFTcard">
                    <img className="NFTImg" src={NFTtest} alt="#" />
                    <h1 className="fs-32 notoBold my-3">숲속의 사슴</h1>
                    <div className="flex justify-center">
                      <h1 className="fs-18 roThin">Made by</h1>
                      <h1 className="fs-18 mx-2 roMid">{localStorage.MyNick}</h1>
                    </div>
                  </li>
                }
              </div>
            )
          })}
        </ul>
        :
      <ul className="flex justify-center">
        <h1 className="text-center my-5 fs-18 notoBold">수집된 NFT가 없습니다.</h1>
      </ul>
      
      }
<MakeNFTModal open={openModal} close={closeModal} animal={Animal} />
    </div >
  )
}
export default MyNFT;