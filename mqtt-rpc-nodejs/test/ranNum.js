/*******************************************************************************
 *******************************************************************************/

function randomInt (low, high) {
    return Math.floor(Math.random() * (high - low) + low);
}

for (var i =0; i < 100; ++i)
    console.log(randomInt(1,10000));